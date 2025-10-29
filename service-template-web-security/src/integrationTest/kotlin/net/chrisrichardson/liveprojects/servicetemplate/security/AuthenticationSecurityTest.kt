package net.chrisrichardson.liveprojects.servicetemplate.security

import io.restassured.RestAssured.given
import net.chrisrichardson.liveprojects.servicetemplate.security.keycloak.JwtProvider
import net.chrisrichardson.liveprojects.servicetemplate.security.keycloak.RestAssuredUtils.givenJwt
import net.chrisrichardson.liveprojects.servicetemplate.web.CreateAccountRequest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ContextConfiguration(classes = arrayOf(SecurityTestConfiguration::class))
@AutoConfigureMetrics
@TestPropertySource(locations = ["/application-keycloak-test.properties"])
class AuthenticationSecurityTest @Autowired constructor(val jwtProvider : JwtProvider) : AbstractSecurityTest() {

    @Test
    fun `anonymous user GET by account id should get 401`() {
        given().redirects().follow(false).get("http://localhost:$port/accounts/{id}", 101L).then().log().ifValidationFails().statusCode(HttpStatus.UNAUTHORIZED.value())
        verifyNoInteractions(accountRepository)
    }

    @Test
    fun `anonymous user POST to create should get 403`() {
        given().body(CreateAccountRequest(101))
                .contentType("application/json")
                .post("http://localhost:$port/accounts").then().log().ifValidationFails().statusCode(HttpStatus.UNAUTHORIZED.value())
        verifyNoInteractions(accountRepository)
    }

    val invalidJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

    @Test
    fun `user with invalid jwt should get 401`() {

        givenJwt(invalidJwt).get("http://localhost:$port/accounts/{id}", 101L).then().log().ifValidationFails().statusCode(HttpStatus.UNAUTHORIZED.value())
        verifyNoInteractions(accountRepository)
    }

    @Test
    fun `user with valid jwt in service-template role should get 200`() {
        givenJwt(jwtProvider.jwtForAuthorizedUser()).log().ifValidationFails()
                .get("http://localhost:$port/accounts").then().log().ifValidationFails().statusCode(200)
        verify(accountRepository).findByOwner(anyString())
        verifyNoMoreInteractions(accountRepository)
    }



}
