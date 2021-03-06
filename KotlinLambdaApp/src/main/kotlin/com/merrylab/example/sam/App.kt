package com.merrylab.example.sam

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.util.HashMap
import java.io.IOException
import java.sql.ResultSet

/**
 * Handler for requests to Lambda function.
 */
class App : RequestHandler<APIGatewayProxyRequestEvent?, APIGatewayProxyResponseEvent> {

    companion object {
        // Logger
        private val logger = KotlinLogging.logger { }
    }

    override fun handleRequest(input: APIGatewayProxyRequestEvent?, context: Context?): APIGatewayProxyResponseEvent {
        val connection = getConnection()
        val statement = connection.createStatement()
        // DBからJJUGのセッションデータを取得する
        val rs: ResultSet = statement.executeQuery(
            "SELECT title, category, speaker, start_time FROM jjug_content WHERE year = 2021 AND season = 'spring'"
        )
        val contents: List<JjugContent> = convertList(rs)

        // カテゴリ別のMapへ変換する
        val responseData = categorizeContents(contents)

        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        headers["X-Custom-Header"] = "application/json"
        val response = APIGatewayProxyResponseEvent()
            .withHeaders(headers)
        return try {
            response
                .withStatusCode(200)
                .withBody(Json.encodeToString(responseData))
        } catch (e: IOException) {
            response
                .withBody("{}")
                .withStatusCode(500)
        }
    }

    fun categorizeContents(contents: List<JjugContent>) =
        contents.groupBy { content: JjugContent ->
            content.category
        }.mapValues { categoryContents ->
            categoryContents.value.sortedBy { it.start_time }
        }
}

fun convertList(rs: ResultSet): List<JjugContent> {
    val contents = mutableListOf<JjugContent>()
    while (rs.next()) {
        contents.add(
            JjugContent(
                rs.getString("title"), rs.getString("category"),
                rs.getString("speaker"), rs.getLong("start_time")
            )
        )
    }
    return contents
}