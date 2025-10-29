package net.chrisrichardson.liveprojects.servicetemplate.domain

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(classes = [AccountServiceIntegrationTest.Config::class])
class AccountServiceIntegrationTest @Autowired constructor(accountService : AccountService) {

    @Configuration
    @ComponentScan
    class Config {

    }

    @MockBean
    lateinit var accountRepository: AccountRepository

    @Test
    fun shouldConfigure() {

    }

}