package net.chrisrichardson.liveprojects.servicetemplate.testcontainers

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

object ZipkinContainer : DefaultPropertyProvidingContainer() {

    override val container = GenericContainer<Nothing>("openzipkin/zipkin:2.23").apply {
        withReuse(true)
        withExposedPorts(9411)

        ContainerNetwork.withNetwork(this)
        withNetworkAliases(getContainerAlias())

        waitingFor(Wait.forHttp("/api/v2/spans?serviceName=anything"))
    }

    private fun getContainerAlias() = "zipkin"

    override fun consumeProperties(registry: PropertyConsumer) {
        val port = getPort()
        registry.forNameAndPorts(getContainerAlias(), hostPort= getPort(), servicePort=9411)
        registry.add("spring.zipkin.baseUrl", "http://localhost:$port")

    }

    fun getPort() : Int = container.getMappedPort(9411)

    fun getTracesUrl(applicationName : String) = "http://localhost:${getPort()}/api/v2/traces?$applicationName"


}