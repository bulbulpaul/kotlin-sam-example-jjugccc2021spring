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
        assertEquals(result.getStatusCode().toInt(), 200)
        assertEquals(result.getHeaders().get("Content-Type"), "application/json")
        val content: String = result.getBody()
        assertNotNull(content)
        assertTrue(content.contains("\"message\""))
        assertTrue(content.contains("\"hello world\""))
        assertTrue(content.contains("\"location\""))
    }
}