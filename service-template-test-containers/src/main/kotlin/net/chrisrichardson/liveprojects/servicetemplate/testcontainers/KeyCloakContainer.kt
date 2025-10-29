package net.chrisrichardson.liveprojects.servicetemplate.testcontainers

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.ImageFromDockerfile
import java.nio.file.FileSystems

object KeyCloakContainer : DefaultPropertyProvidingContainer() {

    override val container = GenericContainer<Nothing>(ImageFromDockerfile()
            .withDockerfile(FileSystems.getDefault().getPath("../keycloak/Dockerfile"))).apply {
        withReuse(true)
        withEnv("KEYCLOAK_ADMIN", "admin")
        withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
        withEnv("DB_VENDOR", "h2")

        withExposedPorts(8091)
        ContainerNetwork.withNetwork(this)
        withNetworkAliases(getContainerAlias())
        waitingFor(Wait.forHttp("/admin/"))

    }

    private fun getContainerAlias() = "keycloak"

    override fun consumeProperties(registry: PropertyConsumer) {
        val port = getPort()
        val issuerUrl = "http://localhost:$port/realms/${getRealm()}"


        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", issuerUrl)
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", "http://localhost:$port/realms/${getRealm()}/protocol/openid-connect/certs")
        registry.add("keycloak.auth-server-url", getKeyCloakUrl())

        registry.forNameAndPorts(getContainerAlias(), hostPort= getPort(), servicePort=8091)
    }

    fun getKeyCloakUrl(): String {
        val port = getPort()
        return "http://localhost:$port"
    }

    fun getPort() = container.getMappedPort(8091)

    fun getRealm(): String {
        return "service-template"
    }


}