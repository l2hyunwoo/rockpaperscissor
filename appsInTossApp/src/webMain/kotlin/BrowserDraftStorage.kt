import io.github.l2hyunwoo.rps.DraftStorage
import kotlinx.browser.window

class BrowserDraftStorage : DraftStorage {
    override fun read(id: String): String? = window.localStorage.getItem("cmp-note-$id")
    override fun write(id: String, body: String) {
        window.localStorage.setItem("cmp-note-$id", body)
    }
}
