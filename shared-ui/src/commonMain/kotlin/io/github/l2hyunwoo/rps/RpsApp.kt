package io.github.l2hyunwoo.rps

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.l2hyunwoo.rps.resources.NotoSansKR
import io.github.l2hyunwoo.rps.resources.Res
import io.github.l2hyunwoo.rps.resources.hand_paper
import io.github.l2hyunwoo.rps.resources.hand_rock
import io.github.l2hyunwoo.rps.resources.hand_scissors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource

private object Palette {
    val ink = Color(0xFF191F28)
    val muted = Color(0xFF8B95A1)
    val canvas = Color(0xFFF2F4F6)
    val surface = Color(0xFFFFFFFF)
    val primary = Color(0xFF3469D9)
    val danger = Color(0xFFF04452)
}

private val Hand.image: DrawableResource
    get() = when (this) {
        Hand.ROCK -> Res.drawable.hand_rock
        Hand.PAPER -> Res.drawable.hand_paper
        Hand.SCISSORS -> Res.drawable.hand_scissors
    }

private val Outcome.color: Color
    get() = when (this) {
        Outcome.WIN -> Palette.primary
        Outcome.LOSE -> Palette.danger
        Outcome.DRAW -> Palette.muted
    }

/**
 * [game] is null while the host resolves the user key and loads the record.
 * Host callbacks are optional so the screen works without Apps in Toss capabilities.
 */
@Composable
fun RpsApp(
    game: RpsGame?,
    nickname: String? = null,
    exitRequested: Boolean = false,
    onExitRequest: () -> Unit = {},
    onExitConfirm: () -> Unit = {},
    onExitDismiss: () -> Unit = {},
    onHandPicked: () -> Unit = {},
    onRoundPlayed: (Round, Record) -> Unit = { _, _ -> },
    onShare: ((String) -> Unit)? = null,
    onLeaderboard: (() -> Unit)? = null,
    message: String? = null,
) {
    val family = notoSansKr()
    MaterialTheme(colorScheme = lightColorScheme(primary = Palette.primary, background = Palette.canvas, surface = Palette.surface, onSurface = Palette.ink)) {
        Box(Modifier.fillMaxSize().background(Palette.canvas)) {
            Column(Modifier.fillMaxHeight().widthIn(max = 480.dp).fillMaxWidth().align(Alignment.TopCenter).safeDrawingPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
                Header(nickname, family, onExitRequest)
                if (game == null) {
                    Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                        Text("기록을 불러오고 있어요", style = TextStyle(fontFamily = family, fontSize = 15.sp, color = Palette.muted))
                    }
                } else {
                    Game(game, family, onHandPicked, onRoundPlayed, onShare, onLeaderboard, Modifier.weight(1f))
                }
                message?.let {
                    Text(it, Modifier.padding(top = 8.dp), style = TextStyle(fontFamily = family, fontSize = 13.sp, color = Palette.danger))
                }
            }
        }
        if (exitRequested) {
            AlertDialog(
                onDismissRequest = onExitDismiss,
                containerColor = Palette.surface,
                title = { Text("게임을 그만할까요?", style = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Palette.ink)) },
                text = { Text("기록은 저장돼 있어요.", style = TextStyle(fontFamily = family, fontSize = 15.sp, color = Palette.muted)) },
                confirmButton = { TextButton(onClick = onExitConfirm) { Text("그만하기", style = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, color = Palette.ink)) } },
                dismissButton = { TextButton(onClick = onExitDismiss) { Text("계속하기", style = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, color = Palette.muted)) } },
            )
        }
    }
}

@Composable
private fun Header(nickname: String?, family: FontFamily, onExitRequest: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            nickname?.let { "${it}님의 대결" } ?: "가위바위보",
            Modifier.weight(1f),
            style = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Palette.ink),
            maxLines = 1,
        )
        Text(
            "나가기",
            Modifier.offset(x = 12.dp).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClick = onExitRequest).padding(horizontal = 12.dp, vertical = 12.dp),
            style = TextStyle(fontFamily = family, fontSize = 15.sp, color = Palette.muted),
        )
    }
}

private enum class Phase { Idle, Shaking, Revealed }

@Composable
private fun Game(
    game: RpsGame,
    family: FontFamily,
    onHandPicked: () -> Unit,
    onRoundPlayed: (Round, Record) -> Unit,
    onShare: ((String) -> Unit)?,
    onLeaderboard: (() -> Unit)?,
    modifier: Modifier,
) {
    val scope = rememberCoroutineScope()
    var record by remember(game) { mutableStateOf(game.record) }
    var round by remember(game) { mutableStateOf<Round?>(null) }
    var picked by remember(game) { mutableStateOf<Hand?>(null) }
    var phase by remember(game) { mutableStateOf(Phase.Idle) }
    var saveFailed by remember(game) { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    val shake = remember { Animatable(0f) }

    fun pick(hand: Hand) {
        if (phase == Phase.Shaking) return
        onHandPicked()
        picked = hand
        phase = Phase.Shaking
        scope.launch {
            repeat(3) {
                shake.animateTo(-18f, tween(110))
                shake.animateTo(0f, tween(110))
            }
            val played = game.play(hand)
            round = played
            record = game.record
            saveFailed = game.saveFailed
            phase = Phase.Revealed
            onRoundPlayed(played, game.record)
        }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Scoreboard(record, family, game.persists, saveFailed)
        Arena(round.takeIf { phase == Phase.Revealed }, phase, shake.value, family, Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(Hand.SCISSORS, Hand.ROCK, Hand.PAPER).forEach { hand ->
                HandButton(hand, family, selected = phase != Phase.Idle && picked == hand, enabled = phase != Phase.Shaking, onClick = { pick(hand) }, Modifier.weight(1f))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            onLeaderboard?.let { ActionText("랭킹 보기", family, it) }
            onShare?.let { share -> ActionText("기록 공유", family, { share("가위바위보 ${record.summary()}, 최고 ${record.bestStreak}연승") }) }
            ActionText("기록 초기화", family, { confirmReset = true })
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = Palette.surface,
            title = { Text("기록을 초기화할까요?", style = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Palette.ink)) },
            text = { Text("승·패·무와 연승 기록이 모두 0이 돼요.", style = TextStyle(fontFamily = family, fontSize = 15.sp, color = Palette.muted)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    scope.launch {
                        game.reset()
                        record = game.record
                        round = null
                        picked = null
                        phase = Phase.Idle
                        saveFailed = game.saveFailed
                    }
                }) { Text("초기화", style = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, color = Palette.ink)) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("취소", style = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, color = Palette.muted)) } },
        )
    }
}

@Composable
private fun Scoreboard(record: Record, family: FontFamily, persists: Boolean, saveFailed: Boolean) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Stat("승", record.wins, family)
            Stat("패", record.losses, family)
            Stat("무", record.draws, family)
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text("${record.streak}연승", style = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Palette.ink))
                Text("최고 ${record.bestStreak}연승", style = TextStyle(fontFamily = family, fontSize = 13.sp, color = Palette.muted))
            }
        }
        when {
            saveFailed -> Text("기록을 저장하지 못했어요", style = TextStyle(fontFamily = family, fontSize = 13.sp, color = Palette.danger))
            !persists -> Text("이번 실행에서만 기록이 유지돼요", style = TextStyle(fontFamily = family, fontSize = 13.sp, color = Palette.muted))
        }
    }
}

@Composable
private fun Stat(label: String, value: Int, family: FontFamily) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text("$value", style = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Palette.ink))
        Text(label, Modifier.padding(start = 2.dp, bottom = 4.dp), style = TextStyle(fontFamily = family, fontSize = 15.sp, color = Palette.muted))
    }
}

@Composable
private fun Arena(round: Round?, phase: Phase, shake: Float, family: FontFamily, modifier: Modifier) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Palette.surface).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Upright hands stay recognizable; rotating paper or scissors makes them read as other gestures.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Fighter(
                "상대",
                round?.computer ?: Hand.ROCK,
                family,
                glow = Palette.danger.takeIf { round?.outcome == Outcome.LOSE },
                handModifier = Modifier.graphicsLayer {
                    scaleX = -1f
                    translationY = shake
                },
            )
            Text("VS", style = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Palette.muted))
            Fighter("나", round?.player ?: Hand.ROCK, family, glow = Palette.primary.takeIf { round?.outcome == Outcome.WIN }, handModifier = Modifier.graphicsLayer { translationY = shake })
        }
        val (headline, color) = when {
            phase == Phase.Shaking -> "가위 바위 보!" to Palette.ink
            round != null -> round.outcome.label to round.outcome.color
            else -> "무엇을 낼까요?" to Palette.ink
        }
        Text(headline, Modifier.padding(top = 32.dp), style = TextStyle(fontFamily = family, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, color = color))
    }
}

@Composable
private fun Fighter(label: String, hand: Hand, family: FontFamily, glow: Color?, handModifier: Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // The winner's hand sits on a tinted disc so the result shows in the scene, not only in the headline.
        Box(Modifier.size(132.dp).clip(CircleShape).background(glow?.copy(alpha = 0.12f) ?: Color.Transparent), Alignment.Center) {
            Image(painterResource(hand.image), "$label ${hand.label}", handModifier.size(116.dp))
        }
        Side(label, family)
    }
}

@Composable
private fun Side(label: String, family: FontFamily) {
    Text(label, style = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Palette.muted))
}

@Composable
private fun HandButton(hand: Hand, family: FontFamily, selected: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier
            .clip(shape)
            .background(Palette.surface)
            .border(2.dp, if (selected) Palette.ink else Palette.surface, shape)
            .clickable(enabled = enabled, interactionSource = null, indication = if (enabled) LocalIndication.current else null, role = Role.Button, onClickLabel = "${hand.label} 내기", onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painterResource(hand.image), null, Modifier.size(64.dp))
        Text(hand.label, Modifier.padding(top = 4.dp), style = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Palette.ink))
    }
}

@Composable
private fun ActionText(label: String, family: FontFamily, onClick: () -> Unit) {
    Text(
        label,
        Modifier.clip(CircleShape).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 12.dp, vertical = 14.dp),
        style = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Palette.muted),
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun notoSansKr(): FontFamily = FontFamily(
    listOf(400, 700, 800).map { weight ->
        Font(Res.font.NotoSansKR, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))
    },
)
