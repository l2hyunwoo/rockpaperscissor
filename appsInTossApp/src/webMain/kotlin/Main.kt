import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import io.github.l2hyunwoo.ait.*
import io.github.l2hyunwoo.rps.*
import io.github.l2hyunwoo.rps.resources.NotoSansKR
import io.github.l2hyunwoo.rps.resources.Res
import io.github.l2hyunwoo.rps.resources.hand_paper
import io.github.l2hyunwoo.rps.resources.hand_rock
import io.github.l2hyunwoo.rps.resources.hand_scissors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont
import org.jetbrains.compose.resources.preloadImageBitmap

@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "compose-root") {
        // Web resources load asynchronously; drawing before they arrive shows tofu glyphs and empty hands.
        val fonts = listOf(400, 700, 800).map { preloadFont(Res.font.NotoSansKR, FontWeight(it), variationSettings = FontVariation.Settings(FontVariation.weight(it))) }
        val images = listOf(Res.drawable.hand_rock, Res.drawable.hand_paper, Res.drawable.hand_scissors).map { preloadImageBitmap(it) }
        if (fonts.any { it.value == null } || images.any { it.value == null }) {
            Box(Modifier.fillMaxSize().background(Color(0xFFF2F4F6)))
            return@ComposeViewport
        }
        val client = remember { createAppsInTossClient() }
        val scope = rememberCoroutineScope()
        var game by remember { mutableStateOf<RpsGame?>(null) }
        var nickname by remember { mutableStateOf<String?>(null) }
        var exitRequested by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf<String?>(null) }
        var insets by remember { mutableStateOf(SafeAreaInsets(0.0, 0.0, 0.0, 0.0)) }
        DisposableEffect(client) { onDispose { client.close() } }
        LaunchedEffect(client) {
            val userKey = if (client.supports(Capability.USER_ID)) attempt { client.getAnonymousKey() } else null
            val storage = if (client.supports(Capability.STORAGE)) TossStorage(client) else BrowserStorage()
            game = RpsGame.load(storage, userKey)
        }
        LaunchedEffect(client) {
            if (client.supports(Capability.GAME_PROFILE)) {
                nickname = (attempt { client.getGameProfile() } as? GameProfile.Found)?.nickname
            }
        }
        LaunchedEffect(client) {
            if (client.host != RuntimeHost.BROWSER) {
                try {
                    client.events.collect { event ->
                        when (event) {
                            is HostEvent.SafeArea -> insets = event.insets
                            HostEvent.Back -> exitRequested = true
                            HostEvent.Home -> exitRequested = false
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    message = "호스트 이벤트를 연결하지 못했어요."
                }
            }
        }
        val haptic: (HapticType) -> Unit = { type ->
            if (client.supports(Capability.HAPTIC)) scope.launchAttempt { client.generateHaptic(type) }
        }
        // The opaque Toss navigation bar already sits above the WebView, so the status-bar top inset would add a blank band.
        Box(Modifier.fillMaxSize().background(Color(0xFFF2F4F6)).padding(bottom = insets.bottom.dp, start = insets.left.dp, end = insets.right.dp)) {
            RpsApp(
                game = game,
                nickname = nickname,
                exitRequested = exitRequested,
                onExitRequest = { exitRequested = true },
                onExitConfirm = {
                    exitRequested = false
                    scope.launch {
                        message = try {
                            client.closeScreen()
                            null
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            "화면을 닫지 못했어요. 상단의 닫기 버튼을 눌러 주세요."
                        }
                    }
                },
                onExitDismiss = { exitRequested = false },
                onHandPicked = { haptic(HapticType.TAP) },
                onRoundPlayed = { round, record ->
                    haptic(
                        when (round.outcome) {
                            Outcome.WIN -> if (record.streak >= 3) HapticType.CONFETTI else HapticType.SUCCESS
                            Outcome.LOSE -> HapticType.ERROR
                            Outcome.DRAW -> HapticType.TICK_MEDIUM
                        },
                    )
                    // Submit only when this round set a new best streak.
                    if (round.outcome == Outcome.WIN && record.streak == record.bestStreak && client.supports(Capability.LEADERBOARD)) {
                        scope.launchAttempt { client.submitLeaderboardScore(record.bestStreak.toDouble()) }
                    }
                },
                onShare = if (client.supports(Capability.SHARE)) {
                    { text ->
                        scope.launch {
                            message = try {
                                client.shareText(text)
                                null
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                "공유하지 못했어요."
                            }
                        }
                    }
                } else {
                    null
                },
                onLeaderboard = if (client.supports(Capability.LEADERBOARD)) {
                    {
                        scope.launch {
                            message = try {
                                client.openLeaderboard()
                                null
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                "랭킹을 열지 못했어요."
                            }
                        }
                    }
                } else {
                    null
                },
                message = message,
            )
        }
    }
}

/** Optional host features must never break the game, so failures become null. */
private suspend fun <T> attempt(block: suspend () -> T): T? = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (_: Exception) {
    null
}

private fun CoroutineScope.launchAttempt(block: suspend () -> Unit) {
    launch { attempt(block) }
}
