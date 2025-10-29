package net.chrisrichardson.liveprojects.servicetemplate.domain

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import io.restassured.module.mockmvc.RestAssuredMockMvc
import io.restassured.module.mockmvc.RestAssuredMockMvc.config
import io.restassured.module.mockmvc.config.MockMvcConfig.mockMvcConfig
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig
import net.chrisrichardson.liveprojects.servicetemplate.domain.TestData.accountId
import net.chrisrichardson.liveprojects.servicetemplate.web.AccountController
import net.chrisrichardson.liveprojects.servicetemplate.web.CreateAccountRequest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension


@ExtendWith(MockitoExtension::class)
class WebSecurityMockMvcTest {

    @Mock
    lateinit var accountService: AccountService

    fun noSecurity(): RestAssuredMockMvcConfig? {
        return config().mockMvcConfig(mockMvcConfig()
                .dontAutomaticallyApplySpringSecurityMockMvcConfigurer())
    }

    @Test
    fun shouldCreate() {
        val account = Account(TestData.initialBalance, "xyz", accountId)

        whenever(accountService.createAccount(TestData.initialBalance)).thenReturn(AccountServiceCommandResult.Success(account))

        val id : Long = RestAssuredMockMvc.given()
                .config(noSecurity())
                .standaloneSetup(AccountController(accountService))
                .log().ifValidationFails()
                .body(CreateAccountRequest(TestData.initialBalance))
                .contentType("application/json")
                .post("/accounts")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .path("id")

        assertThat(id).isEqualTo(accountId)

    }
}