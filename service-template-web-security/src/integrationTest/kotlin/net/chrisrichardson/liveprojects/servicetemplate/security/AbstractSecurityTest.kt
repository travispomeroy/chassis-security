package net.chrisrichardson.liveprojects.servicetemplate.security

import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountRepository
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountServiceObserver
import net.chrisrichardson.liveprojects.servicetemplate.security.keycloak.JwtProvider
import net.chrisrichardson.liveprojects.servicetemplate.testcontainers.DefaultPropertyProvidingContainer
import net.chrisrichardson.liveprojects.servicetemplate.testcontainers.KeyCloakContainer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

open class AbstractSecurityTest {
    @LocalServerPort
    var port: Int = 0

    @MockBean
    lateinit var accountRepository: AccountRepository

    @MockBean
    lateinit var accountServiceObserver: AccountServiceObserver

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun containerPropertiesConfig(registry: DynamicPropertyRegistry) {
            DefaultPropertyProvidingContainer.startAllAndAddProperties(registry, KeyCloakContainer)
        }
    }

}