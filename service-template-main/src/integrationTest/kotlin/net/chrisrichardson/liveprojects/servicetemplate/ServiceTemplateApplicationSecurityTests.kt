package net.chrisrichardson.liveprojects.servicetemplate

import io.restassured.RestAssured
import net.chrisrichardson.liveprojects.servicetemplate.security.keycloak.JwtProvider
import net.chrisrichardson.liveprojects.servicetemplate.security.keycloak.RestAssuredUtils
import net.chrisrichardson.liveprojects.servicetemplate.web.CreateAccountRequest
import org.hamcrest.CoreMatchers.describedAs
import org.hamcrest.Matchers.oneOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = ["/application-keycloak-test.properties"])
class ServiceTemplateApplicationSecurityTests @Autowired constructor(val jwtProvider : JwtProvider)   {

	@LocalServerPort
	var port: Int = 0

	companion object {
		@RegisterExtension
		@JvmField
		val dockerCompose: DockerComposeHelper = DockerComposeHelper("mysql", "prometheus", "keycloak", "zipkin")
	}


	@Test
	fun `anonymous user POST to create should get 403`() {
		RestAssured.given().body(CreateAccountRequest(101))
				.contentType("application/json")
				.post("http://localhost:$port/accounts").then().log().ifValidationFails().statusCode(HttpStatus.UNAUTHORIZED.value())
	}

	@Test
	fun `user with valid jwt in service-template role should get 200`() {
		RestAssuredUtils.givenJwt(jwtProvider.jwtForAuthorizedUser()).log().ifValidationFails()
				.get("http://localhost:$port/accounts").then().log().ifValidationFails().statusCode(200)
	}

	@Test
	fun `the SwaggerUI should be accessible by anonymous user`() {
		listOf("/swagger-ui.html", "/swagger-ui/index.html", "/v3/api-docs")
				.forEach { RestAssured.get("http://localhost:${port}${it}").then().statusCode(describedAs(it, oneOf(200)))
				}
	}
}
