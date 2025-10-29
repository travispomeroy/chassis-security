package net.chrisrichardson.liveprojects.servicetemplate.testcontainers

import com.github.dockerjava.api.exception.InternalServerErrorException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network

interface PropertyProvidingContainer {

    fun addProperties(registry: DynamicPropertyRegistry)
    fun consumeProperties(registry: DefaultPropertyProvidingContainer.PropertyConsumer)

    fun start()
    fun pause()
    fun unpause()
    fun startAndAddProperties(registry: DynamicPropertyRegistry)
}

object ContainerNetwork {
    private val network : Network? = null // by lazy { Network.SHARED }

    // No networks for now
    fun withNetwork(container: GenericContainer<Nothing>) {
        if (network != null)
            container.withNetwork(network)
    }
}

abstract class DefaultPropertyProvidingContainer : PropertyProvidingContainer {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun startAndAddProperties(registry: DynamicPropertyRegistry) {
        logger.info("Starting {}", this)
        start()
        addProperties(registry)
    }

    companion object {

        fun startAllAndAddProperties(registry: DynamicPropertyRegistry, vararg containers: PropertyProvidingContainer) {
            containers.forEach { c ->
                c.startAndAddProperties(registry)
            }
        }

        fun startAll(vararg containers: PropertyProvidingContainer) {
            containers.forEach { c ->
                c.start()
            }
        }

        fun getPropertiesForClientContainer(vararg containers: PropertyProvidingContainer): Map<String, String> {
            val properties: MutableMap<String, String> = mutableMapOf()
            containers.forEach { c ->
                c.consumeProperties(object : PropertyConsumer {

                    var containerName: String? = null
                    var hostPort: Int? = null
                    var servicePort: Int? = null

                    override fun forNameAndPorts(containerName: String, hostPort: Int, servicePort: Int) {
                        this.containerName = containerName
                        this.hostPort = hostPort
                        this.servicePort = servicePort
                    }

                    override fun add(name: String, value: String) {
                        properties.put(name, value.replace(Regex("localhost:[0-9]+"), "${containerName!!}:${servicePort}").replace("localhost", containerName!!))
                    }
                })
            }
            return properties
        }
    }

    override fun start() {
        container.start()
    }

    override fun pause() {
        container.dockerClient.pauseContainerCmd(container.containerId).exec();
    }

    override fun unpause() {
        try {
            container.dockerClient.unpauseContainerCmd(container.containerId).exec();
        } catch (e: InternalServerErrorException) {
            if (!e.message!!.contains(" is not paused"))
                throw e
        }
    }

    abstract val container: GenericContainer<Nothing>

    interface PropertyConsumer {
        fun forNameAndPorts(containerName: String, hostPort: Int, servicePort: Int)
        fun add(name: String, value: String)
    }

    protected class RegistryConfigurer(val registry: DynamicPropertyRegistry) : PropertyConsumer {

        override fun forNameAndPorts(containerName: String, hostPort: Int, servicePort: Int) {
            // Do nothing
        }

        override fun add(name: String, value: String) {
            registry.add(name) { -> value }
        }
    }

    override fun addProperties(registry: DynamicPropertyRegistry) {
        consumeProperties(RegistryConfigurer(registry))
    }

    override fun consumeProperties(registry: PropertyConsumer) {
        // Do nothing
    }

    protected fun maybeReplaceLocalhost(url: String, forLocalhost: Boolean, containerAlias: String): String = if (forLocalhost) url else url.replace(Regex("localhost:[0-9]+"), containerAlias)
}

