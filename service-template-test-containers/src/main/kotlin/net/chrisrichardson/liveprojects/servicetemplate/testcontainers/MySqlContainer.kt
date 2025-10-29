package net.chrisrichardson.liveprojects.servicetemplate.testcontainers

import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

object MySqlContainer : DefaultPropertyProvidingContainer() {

    fun getContainerAlias() = "mysql"

    override val container: MySQLContainer<Nothing> = MySQLContainer<Nothing>(DockerImageName.parse("mysql/mysql-server:8.0.27-1.2.6-server").asCompatibleSubstituteFor("mysql")).apply {
        withEnv("MYSQL_ROOT_HOST", "%")
        withDatabaseName("dbname")
        withReuse(true)
        ContainerNetwork.withNetwork(this)
        withNetworkAliases(getContainerAlias())
    }



    override fun consumeProperties(registry: PropertyConsumer) {
        registry.forNameAndPorts(getContainerAlias(), hostPort= getPort(), servicePort=3306)

        registry.add("spring.datasource.url", container.getJdbcUrl())
        registry.add("spring.datasource.password", container.getPassword())
        registry.add("spring.datasource.username", container.getUsername())
    }

    fun getPort() = container.getMappedPort(3306)


}
