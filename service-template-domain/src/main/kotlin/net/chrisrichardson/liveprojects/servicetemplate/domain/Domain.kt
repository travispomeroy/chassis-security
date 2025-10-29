package net.chrisrichardson.liveprojects.servicetemplate.domain

import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountCommandResult.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
class Account (var balance : Long,
               var owner : String,
               @Id @GeneratedValue var id : Long? = null) {
    constructor() : this(0, "")

    companion object {
        fun createAccount(balance : Long, owner : String) : AccountCommandResult {
            if (balance <= 0)
                return AmountNotGreaterThanZero(balance)
            else
                return AccountCreationSuccessful(Account(balance, owner))

        }
    }
    fun debit(amount: Long) : AccountCommandResult {
        if (amount <= 0)
            return AmountNotGreaterThanZero(amount)

        if (amount > balance)
            return BalanceExceeded(amount, balance)

        balance -= amount

        return Success
    }

    fun credit(amount: Long) : AccountCommandResult {
        if (amount <= 0)
            return AmountNotGreaterThanZero(amount)

        balance += amount

        return Success
    }

}

sealed class AccountCommandResult {
    data class AccountCreationSuccessful(val account: Account) : AccountCommandResult()
    object Success : AccountCommandResult()
    data class AmountNotGreaterThanZero(val amount : Long) : AccountCommandResult()
    data class BalanceExceeded(val amount : Long, val balance : Long) : AccountCommandResult()
    object Unauthorized : AccountCommandResult()

}

