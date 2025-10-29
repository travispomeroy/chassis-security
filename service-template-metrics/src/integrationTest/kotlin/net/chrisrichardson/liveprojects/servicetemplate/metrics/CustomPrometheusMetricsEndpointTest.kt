package net.chrisrichardson.liveprojects.servicetemplate.metrics

import io.restassured.RestAssured.get
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes=[MetricsTestConfig::class])
@AutoConfigureMetrics
class CustomPrometheusMetricsEndpointTest  {


    @LocalServerPort
    var port: Int = 0

    @Test
    fun theServiceShouldExposePrometheus() {
        val body = get("http://localhost:$port/actuator/prometheus").then().statusCode(200).extract().body().asString()!!
        assertThat(body).contains("account_created_total")
    }


}

