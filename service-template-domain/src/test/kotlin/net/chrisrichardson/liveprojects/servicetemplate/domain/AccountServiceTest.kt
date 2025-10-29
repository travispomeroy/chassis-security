package net.chrisrichardson.liveprojects.servicetemplate.domain

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class AccountServiceTest {


    @Mock
    lateinit var accountRepository: AccountRepository

    @Mock
    lateinit var authenticatedUserSupplier: AuthenticatedUserSupplier

    @Mock
    lateinit var accountServiceObserver: AccountServiceObserver

    private lateinit var accountService: AccountServiceImpl

    @BeforeEach
    fun setUp() {
       accountService = AccountServiceImpl(accountRepository, authenticatedUserSupplier, accountServiceObserver)
    }

    val authenticatedUser = AuthenticatedUser("user-1010", setOf())

    @Test
    fun shouldCreate() {
        whenever(authenticatedUserSupplier.get()).thenReturn(authenticatedUser)

        val savedAccount = ArgumentCaptor.forClass(Account::class.java);

        whenever(accountRepository.save(savedAccount.capture())).thenAnswer { invocation -> invocation.getArgument<Account>(0) }

        val outcome = accountService.createAccount(TestData.initialBalance)

        Assertions.assertThat(outcome).isEqualTo(AccountServiceCommandResult.Success(savedAccount.value))
    }

}