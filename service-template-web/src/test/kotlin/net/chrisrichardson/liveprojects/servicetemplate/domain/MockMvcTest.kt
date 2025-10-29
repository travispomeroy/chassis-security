package net.chrisrichardson.liveprojects.servicetemplate.domain

import org.mockito.kotlin.whenever
import io.restassured.module.mockmvc.RestAssuredMockMvc
import net.chrisrichardson.liveprojects.servicetemplate.domain.TestData.accountId
import net.chrisrichardson.liveprojects.servicetemplate.web.AccountController
import net.chrisrichardson.liveprojects.servicetemplate.web.CreateAccountRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension


@ExtendWith(MockitoExtension::class)
class MockMvcTest {

    @Mock
    lateinit var accountService: AccountService

    @Test
    fun shouldCreate() {
        val account = Account(TestData.initialBalance, "xyz", accountId)

        whenever(accountService.createAccount(TestData.initialBalance)).thenReturn(AccountServiceCommandResult.Success(account))

        val id : Long = RestAssuredMockMvc.given()
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