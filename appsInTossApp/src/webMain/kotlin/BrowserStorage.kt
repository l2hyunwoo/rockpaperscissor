import io.github.l2hyunwoo.ait.AppsInTossClient
import io.github.l2hyunwoo.rps.KeyValueStorage
import kotlinx.browser.window

class BrowserStorage : KeyValueStorage {
    override suspend fun read(key: String): String? = window.localStorage.getItem(key)
    override suspend fun write(key: String, value: String) {
        window.localStorage.setItem(key, value)
    }
}

/** Toss app storage survives WebView data clearing that can drop localStorage. */
class TossStorage(private val client: AppsInTossClient) : KeyValueStorage {
    override suspend fun read(key: String): String? = client.getStorageItem(key)
    override suspend fun write(key: String, value: String) = client.setStorageItem(key, value)
}
