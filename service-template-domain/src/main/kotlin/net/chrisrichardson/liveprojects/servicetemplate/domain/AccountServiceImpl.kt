package net.chrisrichardson.liveprojects.servicetemplate.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.function.Supplier

interface AccountService {
    fun createAccount(initialBalance: Long): AccountServiceCommandResult
    fun findAccount(id: Long): AccountServiceCommandResult
    fun debit(id: Long, amount: Long): AccountServiceCommandResult
    fun credit(id: Long, amount: Long): AccountServiceCommandResult
    fun findAllAccounts(): List<Account>
}

@Service
@Transactional
class AccountServiceImpl @Autowired constructor(val accountRepository: AccountRepository,
                                                val authenticatedUserSupplier: AuthenticatedUserSupplier = AuthenticatedUserSupplier.EMPTY_SUPPLIER,
                                                val accountServiceObserver: AccountServiceObserver?) : AccountService {


    override fun createAccount(initialBalance: Long): AccountServiceCommandResult =
        when (val outcome = Account.createAccount(initialBalance, currentUserId())) {
            is AccountCommandResult.AccountCreationSuccessful -> {
                accountServiceObserver?.noteAccountCreated()
                AccountServiceCommandResult.Success(accountRepository.save(outcome.account))
            }
            is AccountCommandResult.AmountNotGreaterThanZero ->
                AccountServiceCommandResult.AmountNotGreaterThanZero(initialBalance)
            else ->
                AccountServiceCommandResult.Unexpected(outcome)
        }

    private fun currentUserId(): String = authenticatedUserSupplier.get().id

    override fun findAccount(id: Long): AccountServiceCommandResult {
        return withAuthorizedAccess(id) { account -> AccountServiceCommandResult.Success(account) }
            .orElseGet { ->
                AccountServiceCommandResult.AccountNotFound
            }
    }

    override fun debit(id: Long, amount: Long): AccountServiceCommandResult {
        return withAuthorizedAccess(id) { account ->
            when (val outcome = account.debit(amount)) {
                is AccountCommandResult.Success -> {
                    accountServiceObserver?.noteSuccessfulDebit()
                    AccountServiceCommandResult.Success(account)
                }
                is AccountCommandResult.AmountNotGreaterThanZero -> {
                    accountServiceObserver?.noteFailedDebit()
                    AccountServiceCommandResult.AmountNotGreaterThanZero(amount)
                }
                is AccountCommandResult.BalanceExceeded -> {
                    accountServiceObserver?.noteFailedDebit()
                    AccountServiceCommandResult.BalanceExceeded(amount, account.balance)
                }
                is AccountCommandResult.Unauthorized -> {
                    accountServiceObserver?.noteFailedDebit()
                    AccountServiceCommandResult.Unauthorized
                }
                else ->
                    AccountServiceCommandResult.Unexpected(outcome)
            }
        }.orElseGet { ->
            accountServiceObserver?.noteFailedDebit()
            AccountServiceCommandResult.AccountNotFound
        }
    }


    override fun credit(id: Long, amount: Long): AccountServiceCommandResult {
        return withAuthorizedAccess(id) { account ->
            when (val outcome = account.credit(amount)) {
                is AccountCommandResult.Success -> {
                    accountServiceObserver?.noteSuccessfulCredit()
                    AccountServiceCommandResult.Success(account)
                }
                is AccountCommandResult.AmountNotGreaterThanZero -> {
                    accountServiceObserver?.noteFailedCredit()
                    AccountServiceCommandResult.AmountNotGreaterThanZero(amount)
                }
                is AccountCommandResult.Unauthorized -> {
                    accountServiceObserver?.noteFailedDebit()
                    AccountServiceCommandResult.Unauthorized
                }
                else ->
                    AccountServiceCommandResult.Unexpected(outcome)
            }
        }.orElseGet { ->
            accountServiceObserver?.noteFailedCredit()
            AccountServiceCommandResult.AccountNotFound
        }
    }

    override fun findAllAccounts(): List<Account> {
        val result: MutableList<Account> = mutableListOf()
        accountRepository.findByOwner(currentUserId()).toCollection(result)
        return result
    }

    private fun withAuthorizedAccess(id: Long, function: (account: Account) -> AccountServiceCommandResult)
            : Optional<AccountServiceCommandResult> {
        return accountRepository.findById(id).map { account ->
            if (account.owner != currentUserId())
                AccountServiceCommandResult.Unauthorized
            else
                function(account)
        }
    }
}

interface AccountServiceObserver {

    fun noteAccountCreated()
    fun noteSuccessfulDebit()
    fun noteFailedDebit()
    fun noteFailedCredit()
    fun noteSuccessfulCredit()
    fun noteUnauthorizedAccountAccess()
}

sealed class AccountServiceCommandResult {
    data class Success(val account: Account) : AccountServiceCommandResult()
    data class AmountNotGreaterThanZero(val amount: Long) : AccountServiceCommandResult()
    data class BalanceExceeded(val amount: Long, val balance: Long) : AccountServiceCommandResult()
    data class Unexpected(val outcome: AccountCommandResult) : AccountServiceCommandResult()

    object AccountNotFound : AccountServiceCommandResult()
    object Unauthorized : AccountServiceCommandResult()

}


interface AuthenticatedUserSupplier : Supplier<AuthenticatedUser> {
    object EMPTY_SUPPLIER : AuthenticatedUserSupplier {
        override fun get() = AuthenticatedUser("nullId", emptySet())

    }
}

data class AuthenticatedUser(val id: String, val roles: Set<String>)

