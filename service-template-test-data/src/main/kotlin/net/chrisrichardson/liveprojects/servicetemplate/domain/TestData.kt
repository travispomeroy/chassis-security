package net.chrisrichardson.liveprojects.servicetemplate.domain

object TestData {

    val accountId = 99L

    val initialBalance = 101L
    val debitAmount = 11L
    val creditAmount: Long = 5L
    val balanceAfterDebit = initialBalance - debitAmount
    val balanceAfterCredit = balanceAfterDebit + creditAmount

}