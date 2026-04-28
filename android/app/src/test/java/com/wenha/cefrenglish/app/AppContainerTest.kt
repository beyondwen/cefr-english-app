package com.wenha.cefrenglish.app

import org.junit.Assert.assertTrue
import org.junit.Test

class AppContainerTest {
    @Test
    fun createCefrHttpClient_allowsSlowAiGenerationRequests() {
        val client = createCefrHttpClient(apiToken = "test-token")

        assertTrue(client.callTimeoutMillis >= 60_000)
        assertTrue(client.readTimeoutMillis >= 60_000)
    }
}
