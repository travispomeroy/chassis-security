package net.chrisrichardson.liveprojects.servicetemplate.webtests

import org.mockito.kotlin.whenever
import io.restassured.RestAssured.given
import net.chrisrichardson.liveprojects.servicetemplate.domain.Account
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountService
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountServiceCommandResult
import net.chrisrichardson.liveprojects.servicetemplate.domain.TestData
import net.chrisrichardson.liveprojects.servicetemplate.util.UtilConfiguration
import net.chrisrichardson.liveprojects.servicetemplate.web.AccountController
import net.chrisrichardson.liveprojects.servicetemplate.web.AmountRequest
import net.chrisrichardson.liveprojects.servicetemplate.web.CreateAccountRequest
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.*
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [WebTest.Config::class])
@EnableAutoConfiguration
@ContextConfiguration(classes = [WebTest.Config::class])
class WebTest {

    @Configuration
    @Import(value=[UtilConfiguration::class])
    @ComponentScan(basePackageClasses = [AccountController::class, AccountService::class])
    class Config

    @LocalServerPort
    var port: Int = 0

    @MockBean
    lateinit var accountService: AccountService

    private val testAccount = Account(TestData.initialBalance, "owner", TestData.accountId)

    private val successfulCreation = AccountServiceCommandResult.Success(testAccount)

    @Test
    fun shouldCreateAccountEtc() {

        whenever(accountService.createAccount(TestData.initialBalance)).thenReturn(successfulCreation)
        val id = createAccount()

        assertThat(id).isEqualTo(TestData.accountId)

        verify(accountService).createAccount(TestData.initialBalance)
    }


    @Test
    fun debitGreaterThanBalanceShouldFail() {

        whenever(accountService.createAccount(TestData.initialBalance)).thenReturn(successfulCreation)

        val id = createAccount()

        val amount = TestData.initialBalance + 1

        whenever(accountService.debit(id, amount)).thenReturn(AccountServiceCommandResult.BalanceExceeded(TestData.initialBalance, amount))

        debitAccount(TestData.accountId, 409, amount)
                .assertThat()
                .body("message", not(nullValue()))

        verify(accountService).debit(id, amount)
    }

    @Test
    fun creditAccount() {

        whenever(accountService.createAccount(TestData.initialBalance)).thenReturn(successfulCreation)

        val id = createAccount()

        val amount = -1L

        whenever(accountService.credit(id, amount)).thenReturn(AccountServiceCommandResult.AmountNotGreaterThanZero(amount))

        creditAccount(TestData.accountId, 409, amount)
                .assertThat()
                .body("message", not(nullValue()))

        verify(accountService).credit(id, amount)
    }

    private fun creditAccount(id: Long, expectedStatus: Int, amount: Long) = given()
            .body(AmountRequest(amount))
            .contentType("application/json")
            .put("http://localhost:$port/accounts/{id}/credit", id)
            .then()
            .statusCode(expectedStatus)

    private fun debitAccount(id: Long, expectedStatus: Int, amount: Long) = given()
            .body(AmountRequest(amount))
            .contentType("application/json")
            .put("http://localhost:$port/accounts/{id}/debit", id)
            .then()
            .statusCode(expectedStatus)

    private fun createAccount(): Long {
        val id: Long = given()
                .body(CreateAccountRequest(TestData.initialBalance))
                .contentType("application/json")
                .post("http://localhost:$port/accounts")
                .then()
                .statusCode(200)
                .extract()
                .path("id")
        return id
    }


}



