package com.merrylab.example.sam

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AppTest {

    @Test
    fun successfulResponse() {
        val app = App()
        val result: APIGatewayProxyResponseEvent = app.handleRequest(null, null)
        assertEquals(result.statusCode.toInt(), 200)
        assertEquals(result.headers.get("Content-Type"), "application/json")
        val content: String = result.body
        assertNotNull(content)
        assertTrue(content.contains("\"message\""))
        assertTrue(content.contains("\"hello world\""))
        assertTrue(content.contains("\"location\""))
    }
}