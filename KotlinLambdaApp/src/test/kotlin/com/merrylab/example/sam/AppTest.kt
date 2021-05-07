package com.merrylab.example.sam

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.sql.Timestamp

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

    @Nested
    inner class CategoryTest {

        @Test
        fun `カテゴリー別に配列になっていること`() {
            val app = App()
            val contents = listOf(
                JjugContent("タイトルA", "java", "スピーカーA", Timestamp(0)),
                JjugContent("タイトルB", "kotlin", "スピーカーA", Timestamp(1)),
                JjugContent("タイトルC", "kotlin", "スピーカーA", Timestamp(2))
            )
            val result = app.categoriseContents(contents)
            assertEquals(result["kotlin"]?.size, 2)
            assertEquals(result["java"]?.size, 1)
        }
    }
}