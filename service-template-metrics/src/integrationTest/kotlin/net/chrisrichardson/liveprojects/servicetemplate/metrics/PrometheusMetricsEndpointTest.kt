package net.chrisrichardson.liveprojects.servicetemplate.metrics

import io.restassured.RestAssured.get
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes=[MetricsTestConfig::class])
@AutoConfigureMetrics
class PrometheusMetricsEndpointTest  {

    @LocalServerPort
    var port: Int = 0

    @Test
    fun theServiceShouldExposePrometheus() {
        get("http://localhost:$port/actuator/prometheus").then().statusCode(200)
    }


}