package net.chrisrichardson.liveprojects.servicetemplate.distributedtracing

import io.restassured.RestAssured.get
import io.restassured.RestAssured.given
import net.chrisrichardson.liveprojects.servicetemplate.domain.Account
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountService
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountServiceCommandResult
import net.chrisrichardson.liveprojects.servicetemplate.testcontainers.DefaultPropertyProvidingContainer
import net.chrisrichardson.liveprojects.servicetemplate.testcontainers.ZipkinContainer
import net.chrisrichardson.liveprojects.servicetemplate.util.Eventually.eventually
import net.chrisrichardson.liveprojects.servicetemplate.util.UtilConfiguration
import net.chrisrichardson.liveprojects.servicetemplate.web.AccountController
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ContextConfiguration(classes = arrayOf(DistributedTracingTest.Config::class))
class DistributedTracingTest {

    @Configuration
    @EnableAutoConfiguration
    @Import(value = [UtilConfiguration::class])
    @ComponentScan(basePackageClasses = [Config::class, AccountController::class, ])
    class Config


    @LocalServerPort
    var port: Int = 0

    @MockBean
    lateinit var accountService: AccountService

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun containerPropertiesConfig(registry: DynamicPropertyRegistry) {
            DefaultPropertyProvidingContainer.startAllAndAddProperties(registry, ZipkinContainer)
        }

    }


    @Test
    fun shouldRecordTrace() {

        val accountId = System.currentTimeMillis()

        val zipkinUrl = ZipkinContainer.getTracesUrl("annotationQuery=http.path=/accounts/${accountId}")

        System.out.println("Zipkin url=$zipkinUrl")

        net.chrisrichardson.liveprojects.servicetemplate.util.Eventually.withConfiguration(iterations=100) {

            get(zipkinUrl)
                    .then()
                    .statusCode(200)

        }


        val account = Account(105, "Owner", accountId)

        Mockito.`when`(accountService.findAccount(accountId)).thenReturn(AccountServiceCommandResult.Success(account))

        given()
        .log().ifValidationFails()
        .get("http://localhost:$port/accounts/${accountId}").then().log().ifValidationFails().statusCode(200)

        Mockito.verify(accountService).findAccount(accountId)
        Mockito.verifyNoMoreInteractions(accountService)

 
        net.chrisrichardson.liveprojects.servicetemplate.util.Eventually.withConfiguration(iterations=100) {
            val body = get(zipkinUrl)
                    .then()
                    .statusCode(200)
                    .assertThat()
                    .body("size()", greaterThan(0))
                    .extract()
                    .body()
                    .`as`(List::class.java)

            System.out.println("Body=${body}")
        }


    }


}


