package io.github.l2hyunwoo.rps

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.rps.resources.NotoSansKR
import io.github.l2hyunwoo.rps.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun RpsApp(
    userKey: String?,
    loadingUser: Boolean,
    storage: KeyValueStorage = remember { MemoryStorage() },
    exitRequested: Boolean = false,
    onExitRequest: (() -> Unit)? = null,
    onExitConfirm: () -> Unit = {},
    onExitDismiss: () -> Unit = {},
    onShare: ((String) -> Unit)? = null,
    message: String? = null,
) {
    MaterialTheme(colorScheme = lightColorScheme(), typography = serviceTypography()) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.safeDrawingPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("가위바위보", style = MaterialTheme.typography.headlineMedium)
                if (loadingUser) {
                    Text("기록을 불러오고 있어요.")
                } else {
                    Game(remember(userKey, storage) { RpsGame(storage, userKey) }, onShare, Modifier.weight(1f))
                }
                message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                onExitRequest?.let { TextButton(onClick = it) { Text("나가기") } }
            }
        }
        if (exitRequested) {
            AlertDialog(
                onDismissRequest = onExitDismiss,
                title = { Text("게임을 끝낼까요?") },
                confirmButton = { TextButton(onClick = onExitConfirm) { Text("끝내기") } },
                dismissButton = { TextButton(onClick = onExitDismiss) { Text("계속하기") } },
            )
        }
    }
}

@Composable
private fun Game(game: RpsGame, onShare: ((String) -> Unit)?, modifier: Modifier = Modifier) {
    var record by remember(game) { mutableStateOf(game.record) }
    var round by remember(game) { mutableStateOf<Round?>(null) }
    var saveFailed by remember(game) { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("내 기록", style = MaterialTheme.typography.labelLarge)
                Text(record.summary(), style = MaterialTheme.typography.titleLarge)
                if (!game.persists) Text("기록을 저장할 수 없어 이번 실행에서만 보여요.")
                if (saveFailed) Text("기록을 저장하지 못했어요.", color = MaterialTheme.colorScheme.error)
            }
        }
        Column(Modifier.fillMaxWidth().weight(1f), Arrangement.Center, Alignment.CenterHorizontally) {
            val current = round
            if (current == null) {
                Text("하나를 골라 주세요.", style = MaterialTheme.typography.titleMedium)
            } else {
                Text("나 ${current.player.label} · 상대 ${current.computer.label}", style = MaterialTheme.typography.titleMedium)
                Text(current.outcome.label, style = MaterialTheme.typography.displaySmall)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Hand.SCISSORS, Hand.ROCK, Hand.PAPER).forEach { hand ->
                Button(onClick = {
                    round = game.play(hand)
                    record = game.record
                    saveFailed = game.saveFailed
                }, Modifier.weight(1f).heightIn(min = 56.dp)) { Text(hand.label) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { confirmReset = true }, Modifier.weight(1f)) { Text("기록 초기화") }
            onShare?.let { share ->
                OutlinedButton(onClick = { share("가위바위보 기록: ${record.summary()}") }, Modifier.weight(1f)) { Text("기록 공유") }
            }
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("기록을 초기화할까요?") },
            text = { Text("지금까지의 승·패·무 기록이 0이 돼요.") },
            confirmButton = {
                TextButton(onClick = {
                    game.reset()
                    record = game.record
                    round = null
                    saveFailed = game.saveFailed
                    confirmReset = false
                }) { Text("초기화") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("취소") } },
        )
    }
}

@Composable
private fun serviceTypography(): Typography {
    val family = FontFamily(Font(Res.font.NotoSansKR))
    val base = Typography()
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = family),
        displayMedium = base.displayMedium.copy(fontFamily = family),
        displaySmall = base.displaySmall.copy(fontFamily = family),
        headlineLarge = base.headlineLarge.copy(fontFamily = family),
        headlineMedium = base.headlineMedium.copy(fontFamily = family),
        headlineSmall = base.headlineSmall.copy(fontFamily = family),
        titleLarge = base.titleLarge.copy(fontFamily = family),
        titleMedium = base.titleMedium.copy(fontFamily = family),
        titleSmall = base.titleSmall.copy(fontFamily = family),
        bodyLarge = base.bodyLarge.copy(fontFamily = family),
        bodyMedium = base.bodyMedium.copy(fontFamily = family),
        bodySmall = base.bodySmall.copy(fontFamily = family),
        labelLarge = base.labelLarge.copy(fontFamily = family),
        labelMedium = base.labelMedium.copy(fontFamily = family),
        labelSmall = base.labelSmall.copy(fontFamily = family),
    )
}
