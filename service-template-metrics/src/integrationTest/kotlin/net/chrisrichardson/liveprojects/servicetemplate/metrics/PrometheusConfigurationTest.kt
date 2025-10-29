package net.chrisrichardson.liveprojects.servicetemplate.metrics

import io.restassured.RestAssured.get
import net.chrisrichardson.liveprojects.servicetemplate.util.Eventually.eventually
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File

@Testcontainers
class PrometheusConfigurationTest {

    companion object {

        data class ContainerSpec(val hostPort : Int, val containerPort: Int) {
            fun hostUrl(path : String = "") = "http://localhost:${hostPort}${path}"
        }

        val prometheusContainer = ContainerSpec(9090, 9090)


        @JvmStatic
        @Container
        val dockerComposeContainer = DockerComposeContainer<Nothing>(File("../docker-compose.yml")).apply {
            withServices("prometheus")
            withExposedService("prometheus_1", prometheusContainer.containerPort, Wait.forHttp("/api/v1/targets"))
            withBuild(true)
        }
    }

    @Test
    fun shouldConfigureServiceTarget() {

        eventually {

            val targetUrl: String = get(prometheusContainer.hostUrl("/api/v1/targets"))
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("data.activeTargets[0].globalUrl")

            assertThat(targetUrl).isEqualTo("http://host.docker.internal:8080/actuator/prometheus")
        }
    }

}