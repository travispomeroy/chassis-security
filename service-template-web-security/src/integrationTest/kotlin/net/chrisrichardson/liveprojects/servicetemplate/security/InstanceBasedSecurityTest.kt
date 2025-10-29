package net.chrisrichardson.liveprojects.servicetemplate.security

import io.restassured.RestAssured
import org.mockito.kotlin.whenever
import net.chrisrichardson.liveprojects.servicetemplate.domain.Account
import net.chrisrichardson.liveprojects.servicetemplate.security.keycloak.JwtProvider
import net.chrisrichardson.liveprojects.servicetemplate.security.keycloak.RestAssuredUtils.givenJwt
import net.chrisrichardson.liveprojects.servicetemplate.security.keycloak.TestUserCredentials
import net.chrisrichardson.liveprojects.servicetemplate.web.AmountRequest
import net.chrisrichardson.liveprojects.servicetemplate.web.CreateAccountRequest
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ContextConfiguration(classes = arrayOf(SecurityTestConfiguration::class))
@AutoConfigureMetrics
@TestPropertySource(locations = ["/application-keycloak-test.properties"])
class InstanceBasedSecurityTest @Autowired constructor(val jwtProvider : JwtProvider) : AbstractSecurityTest() {


    @Test
    fun `account is owned by creator`() {

        whenever(accountRepository.save(any(Account::class.java))).thenAnswer(object : Answer<Account> {
            override fun answer(p0: InvocationOnMock?): Account {
                val account = p0!!.getArgument<Account>(0)
                account.id = System.currentTimeMillis()
                return account
            }

        })

        givenJwt(jwtProvider.jwtForAuthorizedUser()).log().ifValidationFails()
                .body(CreateAccountRequest(101))
                .contentType("application/json")
                .post("http://localhost:$port/accounts")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("owner", equalTo(jwtProvider.getUserId(TestUserCredentials.userName)))

        verify(accountRepository).save(any(Account::class.java))
        verifyNoMoreInteractions(accountRepository)
    }

    @Test
    fun `creator can credit account`() {

        var savedAccount : Account? = null

        whenever(accountRepository.save(any(Account::class.java))).thenAnswer(object : Answer<Account> {
            override fun answer(p0: InvocationOnMock?): Account {
                val account = p0!!.getArgument<Account>(0)
                account.id = System.currentTimeMillis()
                savedAccount = account
                return account
            }

        })

        val amount = 200L

        givenJwt(jwtProvider.jwtForAuthorizedUser()).log().ifValidationFails()
                .body(CreateAccountRequest(101))
                .contentType("application/json")
                .post("http://localhost:$port/accounts")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("owner", equalTo(jwtProvider.getUserId(TestUserCredentials.userName)))

        whenever(accountRepository.findById(savedAccount?.id!!)).thenReturn(Optional.of(savedAccount!!))

        givenJwt(jwtProvider.jwtForAuthorizedUser()).log().ifValidationFails()
                .body(AmountRequest(amount))
                .contentType("application/json")
                .put("http://localhost:$port/accounts/{id}/credit", savedAccount?.id)
                .then()
                .statusCode(200)

    }

    @Test
    fun `user who is not creator cannot credit account`() {

        var savedAccount : Account? = null

        whenever(accountRepository.save(any(Account::class.java))).thenAnswer(object : Answer<Account> {
            override fun answer(p0: InvocationOnMock?): Account {
                val account = p0!!.getArgument<Account>(0)
                account.id = System.currentTimeMillis()
                savedAccount = account
                return account
            }

        })

        val amount = 200L

        givenJwt(jwtProvider.jwtForAuthorizedUser()).log().ifValidationFails()
                .body(CreateAccountRequest(101))
                .contentType("application/json")
                .post("http://localhost:$port/accounts")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("owner", equalTo(jwtProvider.getUserId(TestUserCredentials.userName)))

        whenever(accountRepository.findById(savedAccount?.id!!)).thenReturn(Optional.of(savedAccount!!))

        givenJwt(jwtProvider.jwtForOtherAuthorizedUser()).log().ifValidationFails()
                .body(AmountRequest(amount))
                .contentType("application/json")
                .put("http://localhost:$port/accounts/{id}/credit", savedAccount?.id)
                .then()
                .statusCode(401)

    }


}
