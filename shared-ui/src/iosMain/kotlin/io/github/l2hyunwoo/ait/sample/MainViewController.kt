@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.experimental.ExperimentalNativeApi::class)

package io.github.l2hyunwoo.rps

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import kotlin.native.ref.WeakReference
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController {
    var host: WeakReference<UIViewController>? = null
    val controller = ComposeUIViewController {
        val storage = remember { UserDefaultsDraftStorage(NSUserDefaults.standardUserDefaults) }
        ServiceApp(storage = storage, onShare = { text ->
            val presenter = host?.get()
            if (presenter != null && presenter.presentedViewController == null) {
                val activity = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
                activity.popoverPresentationController?.let { popover ->
                    popover.sourceView = presenter.view
                    popover.sourceRect = presenter.view.bounds
                }
                presenter.presentViewController(activity, animated = true, completion = null)
            }
        })
    }
    host = WeakReference(controller)
    return controller
}

private class UserDefaultsDraftStorage(private val defaults: NSUserDefaults) : DraftStorage {
    override fun read(id: String): String? = defaults.stringForKey("draft:$id")

    override fun write(id: String, body: String) {
        defaults.setObject(body, forKey = "draft:$id")
    }
}
