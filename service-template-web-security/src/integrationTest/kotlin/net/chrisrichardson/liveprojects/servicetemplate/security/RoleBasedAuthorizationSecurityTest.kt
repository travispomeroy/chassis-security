package net.chrisrichardson.liveprojects.servicetemplate.security

import net.chrisrichardson.liveprojects.servicetemplate.security.keycloak.JwtProvider
import net.chrisrichardson.liveprojects.servicetemplate.security.keycloak.RestAssuredUtils.givenJwt
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
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
class RoleBasedAuthorizationSecurityTest @Autowired constructor(val jwtProvider : JwtProvider) : AbstractSecurityTest() {

    @Test
    fun `user with valid jwt in other role get 403`() {
        givenJwt(jwtProvider.jwtForUserInSomeOtherRole())
                .get("http://localhost:$port/accounts").then().log().ifValidationFails().statusCode(HttpStatus.FORBIDDEN.value())
        verifyNoInteractions(accountRepository)
    }



}
