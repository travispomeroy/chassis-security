package net.chrisrichardson.liveprojects.servicetemplate.webtests

import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import io.restassured.module.mockmvc.RestAssuredMockMvc.given
import net.chrisrichardson.liveprojects.servicetemplate.domain.Account
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountService
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountServiceCommandResult
import net.chrisrichardson.liveprojects.servicetemplate.domain.TestData.initialBalance
import net.chrisrichardson.liveprojects.servicetemplate.util.UtilConfiguration
import net.chrisrichardson.liveprojects.servicetemplate.web.AccountController
import net.chrisrichardson.liveprojects.servicetemplate.web.CreateAccountRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.web.context.WebApplicationContext

@WebMvcTest
@ContextConfiguration(classes = [MockMvcIntegrationTest.MockMvcConfig::class])
class MockMvcIntegrationTest @Autowired constructor(val context: WebApplicationContext) {

    val accountId = 99L

    @MockBean
    lateinit var accountService : AccountService

    @Configuration
    @ComponentScan(basePackageClasses = [AccountController::class])
    @Import(UtilConfiguration::class)
    @Order(101)
    class MockMvcConfig  {


    }


    @Test
    fun shouldInitialize() {
        val account = Account(initialBalance, "xyz", accountId)
        whenever(accountService.createAccount(initialBalance)).thenReturn(AccountServiceCommandResult.Success(account))
        val id = createAccount()
        assertThat(id).isEqualTo(accountId)
        verify(accountService).createAccount(initialBalance)
    }

    private fun createAccount(): Long {
        val id: Long =     given()
                .webAppContextSetup(context)
                .log().ifValidationFails()
                .body(CreateAccountRequest(initialBalance))
                .contentType("application/json")
                .post("/accounts")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract()
                .path("id")
        return id
    }


}




