package com.jamesward.hellokotlinmcpserver

import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.server.McpServer
import io.modelcontextprotocol.server.McpStatelessServerFeatures
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.springaicommunity.mcp.annotation.McpTool
import org.springaicommunity.mcp.annotation.McpToolParam

data class Employee(val name: String, val skills: List<String>)

object MyTools {

    @McpTool(name = "get-skills", description = "the list of all possible employee skills", generateOutputSchema = false)
    fun getSkills(): Set<String> = run {
        println("getSkills")
        SampleData.employees.flatMap { it.skills }.toSet()
    }

    @McpTool(name = "get-employees-with-skill", description = "the list of employees with the specified skill", generateOutputSchema = false)
    fun getEmployeesWithSkill(@McpToolParam(description = "skill", required = true) skill: String): List<Employee> = run {
        println("getEmployeesWithSkill $skill")
        SampleData.employees.filter { employee ->
            employee.skills.any { it.equals(skill, ignoreCase = true) }
        }
    }

}

fun main() {
    val transportProvider = HttpServletStatelessServerTransport.builder().objectMapper(ObjectMapper()).build()

    val toolSpecifications: List<McpStatelessServerFeatures.SyncToolSpecification> = StatelessMcpToolProvider(listOf(MyTools)).toolSpecifications

    val syncServer = McpServer.sync(transportProvider)
        .serverInfo("hello-kotlin-mcp-server", "0.0.1")
        .capabilities(
            ServerCapabilities.builder()
                .tools(false)
                .build()
        )
        .tools(toolSpecifications)
        .build()

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
