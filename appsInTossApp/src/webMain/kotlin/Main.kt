import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import io.github.l2hyunwoo.ait.*
import io.github.l2hyunwoo.rps.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "compose-root") {
        val client = remember { createAppsInTossClient() }
        val scope = rememberCoroutineScope()
        val storage = remember { BrowserStorage() }
        var userKey by remember { mutableStateOf<String?>(null) }
        var loadingUser by remember { mutableStateOf(true) }
        var exitRequested by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf<String?>(null) }
        var insets by remember { mutableStateOf(SafeAreaInsets(0.0, 0.0, 0.0, 0.0)) }
        DisposableEffect(client) { onDispose { client.close() } }
        LaunchedEffect(client) {
            if (client.supports(Capability.USER_ID)) {
                try {
                    userKey = client.getAnonymousKey()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Without a key the game still works but records stay in memory.
                }
            }
            loadingUser = false
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
        Box(Modifier.padding(top = insets.top.dp, bottom = insets.bottom.dp, start = insets.left.dp, end = insets.right.dp)) {
            RpsApp(
                userKey = userKey,
                loadingUser = loadingUser,
                storage = storage,
                exitRequested = exitRequested,
                // The mock bridge never emits Back, so expose the same exit path as a button there.
                onExitRequest = if (client.host == RuntimeHost.MOCK) ({ exitRequested = true }) else null,
                onExitConfirm = {
                    exitRequested = false
                    scope.launch {
                        try {
                            client.closeScreen()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            message = "화면을 닫지 못했어요."
                        }
                    }
                },
                onExitDismiss = { exitRequested = false },
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
                message = message,
            )
        }
    }
}
