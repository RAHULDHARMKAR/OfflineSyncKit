package com.rahuldharmkar.offlinesynckit

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rahuldharmkar.offlinesynckit.api.SyncApiAdapter
import com.rahuldharmkar.offlinesynckit.api.SyncApiResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SyncClientBuilderTest {

    @Test
    fun builderShouldFailWithoutApiAdapter() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val exception = runCatching {
            SyncClient.Builder(context)
                .build()
        }.exceptionOrNull()

        assertNotNull(exception)

        assertEquals(
            """
            SyncApiAdapter is required.

            Example:

            SyncClient.Builder(context)
                .apiAdapter(MyApiAdapter())
                .build()
            """.trimIndent(),
            exception?.message
        )
    }

    @Test
    fun builderShouldCreateSyncKitSuccessfully() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val syncKit = SyncClient.Builder(context)
            .apiAdapter(
                SyncApiAdapter {
                    SyncApiResult(success = true)
                }
            )
            .build()

        assertNotNull(syncKit)
    }
}