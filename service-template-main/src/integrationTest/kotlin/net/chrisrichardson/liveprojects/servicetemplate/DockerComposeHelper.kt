package net.chrisrichardson.liveprojects.servicetemplate

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File

class DockerComposeHelper(vararg services : String) : BeforeAllCallback, AfterAllCallback {

    private val environment : DockerComposeContainer<Nothing> by lazy {
        DockerComposeContainer<Nothing>(File("../docker-compose.yml")).apply {
            withLocalCompose(false)
            withBuild(true)
            withServices(*services)
            if (services.contains("keycloak"))
                withExposedService("keycloak_1", 8091, Wait.forHttp("/")) // It's the JVM port
            withTailChildContainers(true)
        }
    }


    override fun beforeAll(ec: ExtensionContext?) {
        environment.start()
    }

    override fun afterAll(ec: ExtensionContext?) {
        environment.stop()
    }

}