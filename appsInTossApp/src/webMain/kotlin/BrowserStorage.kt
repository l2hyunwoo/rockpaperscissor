import io.github.l2hyunwoo.rps.KeyValueStorage
import kotlinx.browser.window

class BrowserStorage : KeyValueStorage {
    override fun read(key: String): String? = window.localStorage.getItem(key)
    override fun write(key: String, value: String) {
        window.localStorage.setItem(key, value)
    }
}
