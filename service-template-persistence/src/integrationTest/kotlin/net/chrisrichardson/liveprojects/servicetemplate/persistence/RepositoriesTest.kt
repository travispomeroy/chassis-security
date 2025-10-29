package net.chrisrichardson.liveprojects.servicetemplate.persistence

import net.chrisrichardson.liveprojects.servicetemplate.testcontainers.DefaultPropertyProvidingContainer
import net.chrisrichardson.liveprojects.servicetemplate.testcontainers.MySqlContainer
import net.chrisrichardson.liveprojects.servicetemplate.domain.Account
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate


@DataJpaTest
@ContextConfiguration(classes= [RepositoriesTest.Config::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NEVER)
class RepositoriesTest @Autowired constructor(
        val entityManager: TestEntityManager,
        val accountRepository: AccountRepository,
        val transactionTemplate: TransactionTemplate)  {

    @Configuration
    @Import(DomainPersistenceConfiguration::class)
    class Config {

    }
    companion object {

        @JvmStatic
        @DynamicPropertySource
        fun containerPropertiesConfig(registry: DynamicPropertyRegistry) {
            DefaultPropertyProvidingContainer.startAllAndAddProperties(registry, MySqlContainer)
        }

    }


    @Test
    fun `should save and load account`() {
        val initialBalance = 100L

        val account = Account(initialBalance, "xyz")

        transactionTemplate.executeWithoutResult { _ ->
            entityManager.persist(account)
            assertThat(account.id).isNotNull()
        }


        val accountId = account.id!!

        transactionTemplate.executeWithoutResult { _ ->
            val loadedAccount = entityManager.find(Account::class.java, accountId)

            assertThat(loadedAccount.balance).isEqualTo(initialBalance)
        }

        transactionTemplate.executeWithoutResult { _ ->
            val account3 = accountRepository.findById(accountId)
            assertThat(account3.get().balance).isEqualTo(initialBalance)
        }

    }
}