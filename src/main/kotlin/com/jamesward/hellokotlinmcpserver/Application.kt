package com.jamesward.hellokotlinmcpserver

import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.server.McpServer
import io.modelcontextprotocol.server.McpStatelessServerFeatures
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport
import io.modelcontextprotocol.spec.McpSchema
import io.modelcontextprotocol.spec.McpSchema.CallToolResult
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder

@Serializable
data class Employee(val name: String, val skills: List<String>)

object MyTools {

    fun getSkills(): Set<String> = run {
        println("getSkills")
        SampleData.employees.flatMap { it.skills }.toSet()
    }

    fun getEmployeesWithSkill(skill: String): List<Employee> = run {
        println("getEmployeesWithSkill $skill")
        SampleData.employees.filter { employee ->
            employee.skills.any { it.equals(skill, ignoreCase = true) }
        }
    }

}

fun main() {
    val transportProvider = HttpServletStatelessServerTransport.builder().objectMapper(ObjectMapper()).build()

    val syncServer = McpServer.sync(transportProvider)
        .serverInfo("hello-kotlin-mcp-server", "0.0.1")
        .capabilities(
            ServerCapabilities.builder()
                .tools(false)
                .build()
        )
        .build()

    val getSkillsSpec = McpStatelessServerFeatures.SyncToolSpecification.builder()
        .tool(
            McpSchema.Tool.builder()
                .name("getSkills")
                .description("the list of all possible employee skills")
                .inputSchema(
                    McpSchema.JsonSchema(
                        "object",
                        emptyMap(),
                        emptyList(),
                        false,
                        emptyMap(),
                        emptyMap()
                    )
                )
                .build()
        )
        .callHandler { _, _ ->
            val skills = MyTools.getSkills()
            CallToolResult.builder().textContent(listOf(Json.encodeToString(skills))).build()
        }
        .build()

    val getEmployeesWithSkillSpec = McpStatelessServerFeatures.SyncToolSpecification.builder()
        .tool(
            McpSchema.Tool.builder()
                .name("getEmployeesWithSkill")
                .description("get employees that have the specified skill")
                .inputSchema(
                    McpSchema.JsonSchema(
                        "object",
                        mapOf("skill" to mapOf(
                            "type" to "string",
                            "description" to "the skill to search for"
                        )),
                        listOf("skill"),
                        false,
                        emptyMap(),
                        emptyMap()
                    )
                )
                .build()
        )
        .callHandler { _, request ->
            val skill = request.arguments.getValue("skill") as? String
            if (skill == null) {
                CallToolResult.builder().isError(true).build()
            }
            else {
                val employeesWithSkill = MyTools.getEmployeesWithSkill(skill)
                CallToolResult.builder().textContent(listOf(Json.encodeToString(employeesWithSkill))).build()
            }
        }
        .build()

    syncServer.addTool(getSkillsSpec)
    syncServer.addTool(getEmployeesWithSkillSpec)

    val server = Server(8000)
    val context = ServletContextHandler(ServletContextHandler.SESSIONS)
    context.contextPath = "/"
    context.addServlet(ServletHolder(transportProvider), "/mcp")
    server.handler = context

    try {
        server.start()
        println("Server started on http://localhost:8000")
        server.join()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        syncServer.close()
        server.stop()
    }

}
