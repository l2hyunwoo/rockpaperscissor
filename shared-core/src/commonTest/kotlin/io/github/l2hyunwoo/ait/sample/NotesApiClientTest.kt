package io.github.l2hyunwoo.rps

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlinx.coroutines.test.runTest

class NotesApiClientTest {
    @Test fun decodesWithoutUiOrToss() = runTest {
        val api = NotesApiClient { """[{"id":"1","title":"한글","body":"내용"}]""" }
        assertEquals("한글", api.list().single().title)
    }

    @Test fun rejectsInvalidServerData() = runTest {
        assertFails { NotesApiClient { """[{"id":null}]""" }.list() }
    }
}
