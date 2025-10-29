package net.chrisrichardson.liveprojects.servicetemplate.domain

import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountCommandResult.*
import net.chrisrichardson.liveprojects.servicetemplate.domain.TestData.balanceAfterCredit
import net.chrisrichardson.liveprojects.servicetemplate.domain.TestData.balanceAfterDebit
import net.chrisrichardson.liveprojects.servicetemplate.domain.TestData.creditAmount
import net.chrisrichardson.liveprojects.servicetemplate.domain.TestData.debitAmount
import net.chrisrichardson.liveprojects.servicetemplate.domain.TestData.initialBalance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AccountTest {

    @Test
    fun shouldDebitAndCredit() {
        val account = Account(initialBalance, "owner")
        val result = account.debit(debitAmount)

        assertThat(result).isEqualTo(Success)
        assertThat(account.balance).isEqualTo(balanceAfterDebit)

        val creditResult = account.credit(creditAmount)
        assertThat(creditResult).isEqualTo(Success)
        assertThat(account.balance).isEqualTo(balanceAfterCredit)
    }

    @Test
    fun shouldDebitCurrentBalance() {
        val account = Account(initialBalance, "owner")
        val result = account.debit(initialBalance)
        assertThat(result).isEqualTo(Success)
        assertThat(account.balance).isEqualTo(0)
   }

    @Test
    fun shouldDebitCurrentBalanceMinus1() {
        val account = Account(initialBalance, "owner")
        val result = account.debit(initialBalance - 1)
        assertThat(result).isEqualTo(Success)
        assertThat(account.balance).isEqualTo(1)
   }

    @Test
    fun shouldDebitCurrentBalancePlusShouldFail() {
        val account = Account(initialBalance, "owner")
        val result = account.debit(initialBalance + 1)
        assertThat(result).isEqualTo(BalanceExceeded(initialBalance + 1, initialBalance))
        assertThat(account.balance).isEqualTo(initialBalance)
   }

    @Test
    fun debitZeroShouldFail() {
        val account = Account(initialBalance, "owner")
        val amount = 0L
        val result = account.debit(amount)
        assertThat(result).isEqualTo(AmountNotGreaterThanZero(amount))
        assertThat(account.balance).isEqualTo(initialBalance)
   }

    @Test
    fun creditZeroShouldFail() {
        val account = Account(initialBalance, "owner")
        val amount= 0L
        val result = account.credit(amount)
        assertThat(result).isEqualTo(AmountNotGreaterThanZero(amount))
        assertThat(account.balance).isEqualTo(initialBalance)
   }

}