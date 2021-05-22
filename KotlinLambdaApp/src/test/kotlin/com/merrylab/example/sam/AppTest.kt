package com.merrylab.example.sam

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.sql.*

class AppTest {

    @Nested
    inner class CategoryTest {

        @Test
        fun `カテゴリー別に配列になっていること`() {
            val app = App()
            val contents = listOf(
                JjugContent("タイトルA", "java", "スピーカーA", 0),
                JjugContent("タイトルB", "kotlin", "スピーカーA", 1),
                JjugContent("タイトルC", "kotlin", "スピーカーA", 2)
            )
            val result = app.categorizeContents(contents)
            assertEquals(result["kotlin"]?.size, 2)
            assertEquals(result["java"]?.size, 1)
        }
    }

    @Nested
    inner class MockTest {

        private val connection = mockk<Connection>()
        private val statement = mockk<Statement>()
        private val resultSet = mockk<ResultSet>()

        @BeforeEach
        fun prepare() {
            clearAllMocks()
            mockkStatic(::getConnection)
            every { getConnection() } returns connection
            every { connection.createStatement() } returns statement
            every { statement.executeQuery(any()) } returns resultSet
        }

        @Test
        fun `Lambdaを呼び出した際にカテゴリ化したJsonが返ってくること`() {
            val app = App()
            mockkStatic(::convertList)
            every { convertList(any()) } returns listOf(
                JjugContent("タイトルA", "java", "スピーカーA", 0),
                JjugContent("タイトルB", "kotlin", "スピーカーB", 1),
                JjugContent("タイトルC", "kotlin", "スピーカーC", 2)
            )

            val result: APIGatewayProxyResponseEvent = app.handleRequest(null, null)
            assertEquals(result.headers["Content-Type"], "application/json")

            val responseBody = Json.decodeFromString<Map<String, List<JjugContent>>>(result.body)
            assertEquals(responseBody["java"]?.size, 1)
            assertEquals(responseBody["java"]?.first()?.title, "タイトルA")
            assertEquals(responseBody["java"]?.first()?.category, "java")
            assertEquals(responseBody["java"]?.first()?.speaker, "スピーカーA")

            assertEquals(responseBody["kotlin"]?.size, 2)
        }

    }
}