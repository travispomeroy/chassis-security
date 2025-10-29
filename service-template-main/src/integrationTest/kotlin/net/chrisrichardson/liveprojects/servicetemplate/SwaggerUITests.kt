package net.chrisrichardson.liveprojects.servicetemplate

import net.chrisrichardson.liveprojects.servicetemplate.security.keycloak.JwtProvider
import net.chrisrichardson.liveprojects.servicetemplate.util.Eventually
import net.chrisrichardson.liveprojects.servicetemplate.util.Eventually.eventually
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.containers.VncRecordingContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = ["/application-keycloak-test.properties"])
@Testcontainers
class SwaggerUITests @Autowired constructor(val jwtProvider : JwtProvider)   {

    @LocalServerPort
    var port: Int = 0

    companion object {
        @RegisterExtension
        @JvmField
        val dockerCompose: DockerComposeHelper = DockerComposeHelper("mysql", "keycloak")

        @Container
        @JvmField
        val chrome = BrowserWebDriverContainer().withCapabilities(ChromeOptions())
                .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, File("./build"), VncRecordingContainer.VncRecordingFormat.MP4)
    }

    val swaggerUi by lazy { SwaggerUi(chrome.getWebDriver() , port, jwtProvider) }

    @Test
    fun shouldGetAccounts() {
        swaggerUi.openSwaggerUI()

        swaggerUi.authorizeWithJwt()

        swaggerUi.expandGetAccounts()

        swaggerUi.tryItOut()

        swaggerUi.execute()

        swaggerUi.assertThatStatusCodeIsEqualTo("200")

    }

    @Test
    fun shouldNotGetAccountsWithoutJwt() {
        swaggerUi.openSwaggerUI()

        swaggerUi.expandGetAccounts()

        swaggerUi.tryItOut()

        swaggerUi.execute()

        swaggerUi.assertThatStatusCodeIsEqualTo("401\nUndocumented")

    }



}

class SwaggerUi(val driver: RemoteWebDriver, val port : Int, val jwtProvider : JwtProvider) {

    fun openSwaggerUI() {
        val host = System.getenv("DOCKER_HOST_IP") ?: "host.docker.internal"
        driver.get("http://$host:${port}/swagger-ui/index.html")
    }

    fun assertThatStatusCodeIsEqualTo(statusCode: String) {
        eventually { assertThat(driver.findElementByCssSelector("table.responses-table.live-responses-table td[class=\"response-col_status\"]").text).isEqualTo(statusCode) }
    }

    fun execute() {
        (driver as JavascriptExecutor).executeScript("window.scrollBy(0,${driver.findElementByCssSelector("span[data-path='/accounts']").rect.y})")
        waitAndClickByCss("button.btn.execute.opblock-control__btn")
    }

    fun tryItOut() {
        waitAndClickByCss("button.btn.try-out__btn")
    }

    fun expandGetAccounts() {
        waitAndClickByCss("span[data-path='/accounts']")
    }

    fun authorizeWithJwt() {
        waitAndClickByCss("button.authorize.btn.unlocked")
        waitAndSendKeysByCss("div.modal-ux-content input[type=\"text\"]", jwtProvider.jwtForAuthorizedUser())
        waitAndClickByCss("button.btn.modal-btn.auth.authorize.button")
        waitAndClickByCss("button.btn.modal-btn.auth.btn-done.button")
    }

    private fun waitAndClickByCss(selector: String) {
        waitAndDoBy(selector, WebElement::click, driver::findElementByCssSelector)
    }

    private fun waitAndSendKeysByCss(selector: String, text : String) {
        waitAndDoBy(selector, { textField -> textField.sendKeys(text) }, driver::findElementByCssSelector)
    }

    private fun waitAndDoBy(selector: String, thing: (WebElement) -> Unit, finder: (String) -> WebElement) {
        thing(Eventually.withConfiguration(iterations = 30, sleepInMillis = 100) { finder(selector) })
    }
}
