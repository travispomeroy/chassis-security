package net.chrisrichardson.liveprojects.servicetemplate.healthcheck

import io.restassured.RestAssured.get
import net.chrisrichardson.liveprojects.servicetemplate.testcontainers.DefaultPropertyProvidingContainer
import net.chrisrichardson.liveprojects.servicetemplate.testcontainers.MySqlContainer
import net.chrisrichardson.liveprojects.servicetemplate.util.Eventually.eventually
import net.chrisrichardson.liveprojects.servicetemplate.util.UtilConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes= [HealthCheckEndpointTest.Config::class])
class HealthCheckEndpointTest  {

    @Configuration
    @EnableAutoConfiguration
    @Import(value=[UtilConfiguration::class])
    class Config {


    }

    companion object {

        @JvmStatic
        @DynamicPropertySource
        fun containerPropertiesConfig(registry: DynamicPropertyRegistry) {
            DefaultPropertyProvidingContainer.startAllAndAddProperties(registry, MySqlContainer)
        }

    }

    @LocalServerPort
    var port: Int = 0

    @Test
    fun theServiceShouldBeHealthy() {
        eventually {
            get("http://localhost:${port}/actuator/health").then().statusCode(200)

        }
    }

    @Test
    fun theServiceShouldBecomeUnhealthy() {
        MySqlContainer.pause()

        try {
            eventually {
                get("http://localhost:${port}/actuator/health").then().statusCode(503);
            }
        } finally {
            MySqlContainer.unpause()
        }
    }

    @Test
    fun theServiceShouldBecomeHealthyAgain() {
        MySqlContainer.pause()

        try {
            eventually {
                get("http://localhost:${port}/actuator/health").then().statusCode(503);
            }

            MySqlContainer.unpause()

            eventually {
                get("http://localhost:${port}/actuator/health").then().statusCode(200)
            }

        } finally {
            MySqlContainer.unpause()
        }
    }

}