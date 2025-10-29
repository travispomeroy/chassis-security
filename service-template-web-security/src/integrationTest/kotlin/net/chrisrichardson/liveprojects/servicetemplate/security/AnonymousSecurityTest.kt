package net.chrisrichardson.liveprojects.servicetemplate.security

import io.restassured.RestAssured
import org.hamcrest.Matchers.describedAs
import org.hamcrest.Matchers.oneOf
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ContextConfiguration(classes = arrayOf(SecurityTestConfiguration::class))
@AutoConfigureMetrics
@TestPropertySource(locations = ["/application-keycloak-test.properties"])
class AnonymousSecurityTest : AbstractSecurityTest() {

    @Test
    fun theServiceHealthCheckShouldBeAccessible() {
        RestAssured.get("http://localhost:${port}/actuator/health").then().statusCode(200)
    }

    @Test
    fun theServiceMetricsShouldBeAccessible() {
        RestAssured.get("http://localhost:${port}/actuator/prometheus").then().statusCode(200)
    }

    @Test
    fun theSwaggerUIShouldBeAccessibleOrNotFound() {
        listOf("/swagger-ui.html", "/swagger-ui/index.html", "/v3/api-docs/**")
                .forEach {
                    RestAssured.get("http://localhost:${port}${it}").then().statusCode(describedAs(it, oneOf(200, 404)))
                }
    }

}
