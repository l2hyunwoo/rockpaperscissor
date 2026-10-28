@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package io.github.l2hyunwoo.rps

import kotlin.js.JsExport

@JsExport
class BrowserNote(val id: String, val title: String, val body: String)

/** Supply your own fetch/auth transport; parsing uses the shared service model. */
@JsExport
fun decodeNotesResponse(json: String): Array<BrowserNote> = parseNotes(json).map { BrowserNote(it.id, it.title, it.body) }.toTypedArray()
