package net.chrisrichardson.liveprojects.servicetemplate

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServiceTemplateApplicationTests {

	companion object {
		@RegisterExtension
		@JvmField
		val dockerCompose: DockerComposeHelper = DockerComposeHelper("mysql", "prometheus", "zipkin")
	}


	@Test
	fun contextLoads() {
	}

}
