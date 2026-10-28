package io.github.l2hyunwoo.rps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.rps.resources.NotoSansKR
import io.github.l2hyunwoo.rps.resources.Res
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.Font

class ServiceNavigation {
    var selectedId by mutableStateOf<String?>(null)
    fun back(): Boolean {
        if (selectedId == null) return false
        selectedId = null
        return true
    }
}

@Composable
fun ServiceApp(
    state: ServiceNavigation = remember { ServiceNavigation() },
    storage: DraftStorage = remember { MemoryDraftStorage() },
    onShare: ((String) -> Unit)? = null,
    showBackButton: Boolean = true,
    repository: NotesRepository = remember { DemoNotesRepository() },
    hostContent: @Composable () -> Unit = {},
) {
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(repository) {
        try {
            notes = repository.list()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            error = "목록을 불러오지 못했어요."
        }
    }
    MaterialTheme(colorScheme = lightColorScheme(), typography = serviceTypography()) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.safeDrawingPadding().imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("가위바위보", style = MaterialTheme.typography.headlineMedium)
                Text("데모 데이터예요. 필요한 화면과 API를 함께 사용해요.")
                val selected = notes.firstOrNull { it.id == state.selectedId }
                if (selected == null) {
                    error?.let { Text(it) }
                    NotesList(notes, { state.selectedId = it }, Modifier.weight(1f))
                } else {
                    if (showBackButton) TextButton(onClick = { state.back() }) { Text("목록으로 돌아가요") }
                    NoteDetail(selected, Modifier.weight(1f), storage, onShare)
                }
                hostContent()
            }
        }
    }
}

@Composable
fun NotesList(notes: List<Note>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(notes, key = { it.id }) { note ->
            Card(Modifier.fillMaxWidth().clickable(onClickLabel = "상세 보기", onClick = { onSelect(note.id) })) {
                Text(note.title, Modifier.padding(20.dp))
            }
        }
    }
}

@Composable
fun NoteDetail(note: Note, modifier: Modifier = Modifier, storage: DraftStorage = remember { MemoryDraftStorage() }, onShare: ((String) -> Unit)? = null) {
    val loaded = remember(note.id, storage) { runCatching { storage.read(note.id) } }
    var draft by rememberSaveable(note.id) { mutableStateOf(loaded.getOrNull() ?: note.body) }
    var message by remember(note.id) { mutableStateOf<String?>(null) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(note.title, style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(draft, { draft = it }, label = { Text("내용을 입력해요") }, modifier = Modifier.fillMaxWidth())
        if (loaded.isFailure) Text("저장된 내용을 불러오지 못했어요. 다시 열어 주세요.")
        Button(enabled = loaded.isSuccess, onClick = {
            message = try {
                storage.write(note.id, draft)
                "저장했어요."
            } catch (_: Exception) {
                "저장하지 못했어요. 다시 시도해 주세요."
            }
        }) { Text("저장해요") }
        message?.let { Text(it) }
        onShare?.let { share -> TextButton(onClick = { share(draft) }, enabled = draft.isNotBlank()) { Text("내용을 공유해요") } }
        Text("데모는 기기 안에 저장해요. 민감 정보는 입력하지 마세요.")
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
