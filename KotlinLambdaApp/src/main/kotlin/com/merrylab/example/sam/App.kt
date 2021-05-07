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
import kotlin.Throws
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.stream.Collectors

/**
 * Handler for requests to Lambda function.
 */
class App : RequestHandler<APIGatewayProxyRequestEvent?, APIGatewayProxyResponseEvent> {

    companion object {
        // Logger
        private val logger = KotlinLogging.logger {  }
    }

    override fun handleRequest(input: APIGatewayProxyRequestEvent?, context: Context?): APIGatewayProxyResponseEvent {
        val connection = getConnection()
        val statement = connection.createStatement()
        val rs = statement.executeQuery(
            "SELECT title, category, speaker, start_time FROM jjug_content WHERE year = 2021 AND season = 'spring'"
        )
        val contents = mutableListOf<JjugContent>()
        logger.info { "" }
        while (rs.next()) {
            contents.add(JjugContent(rs.getString("title"), rs.getString("category"),
                    rs.getString("speaker"), rs.getTimestamp("start_time") ))
        }

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

    fun categorizeContents(contents: List<JjugContent>): Map<String, List<JjugContent>> {
        return contents.groupBy { content: JjugContent ->
            content.category
        }.mapValues { categoryContents ->
            categoryContents.value.sortedBy { it.start_time }
        }
    }

    @Throws(IOException::class)
    private fun getPageContents(address: String): String {
        val url = URL(address)
        BufferedReader(InputStreamReader(url.openStream())).use { br ->
            return br.lines().collect(Collectors.joining(System.lineSeparator()))
        }
    }
}