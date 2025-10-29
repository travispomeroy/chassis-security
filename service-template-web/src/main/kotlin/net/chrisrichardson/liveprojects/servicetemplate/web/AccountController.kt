package net.chrisrichardson.liveprojects.servicetemplate.web

import net.chrisrichardson.liveprojects.servicetemplate.domain.Account
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountService
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountServiceCommandResult.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class AccountController @Autowired constructor(val accountService: AccountService) {

    @PostMapping("/accounts", produces = ["application/json"])
    fun createAccount(@RequestBody createRequest: CreateAccountRequest) : ResponseEntity<*>  {
        when (val outcome = accountService.createAccount(createRequest.initialBalance)) {
            is Success -> {
                val account = outcome.account
                return ResponseEntity.ok(AccountDTO(account.id!!, account.balance, account.owner))

            }
            is AmountNotGreaterThanZero ->
                return ResponseEntity(ErrorResponse("amount must be greater than zero"), HttpStatus.CONFLICT)
            else ->
                return ResponseEntity(ErrorResponse("Unexpected outcome: ${outcome::class.simpleName}"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/accounts/{id}", produces = ["application/json"])
    fun getAccount(@PathVariable id: Long): ResponseEntity<*> {
        return when (val outcome = accountService.findAccount(id)) {
            is Success -> ResponseEntity.ok(AccountDTO(outcome.account.id!!, outcome.account.balance, outcome.account.owner))
            is AccountNotFound -> ResponseEntity(ErrorResponse("not found ${id}"), HttpStatus.NOT_FOUND)
            else -> ResponseEntity(ErrorResponse("Unexpected outcome: ${outcome::class.simpleName}"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/accounts", produces = ["application/json"])
    fun getAccounts() = AccountsDto(accountService.findAllAccounts().map(this::makeAccountDto))

    private fun makeAccountDto(account: Account) = AccountDTO(account.id!!, account.balance, account.owner)

    @PutMapping("/accounts/{id}/debit", produces = ["application/json"])
    fun debitAccount(@PathVariable id: Long, @RequestBody amountRequest: AmountRequest): ResponseEntity<*> {
        return when (val outcome = accountService.debit(id, amountRequest.amount)) {
            is Success -> ResponseEntity.ok(AccountDTO(outcome.account.id!!, outcome.account.balance, outcome.account.owner))
            is AmountNotGreaterThanZero -> ResponseEntity(ErrorResponse("amount must be greater than zero"), HttpStatus.CONFLICT)
            is BalanceExceeded -> ResponseEntity(ErrorResponse("amount must be greater than zero"), HttpStatus.CONFLICT)
            is AccountNotFound -> ResponseEntity(ErrorResponse("Not found"), HttpStatus.NOT_FOUND)
            is Unauthorized -> ResponseEntity(ErrorResponse("Unauthorized"), HttpStatus.UNAUTHORIZED)
            else -> ResponseEntity(ErrorResponse("Unexpected outcome: ${outcome::class.simpleName}"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PutMapping("/accounts/{id}/credit", produces = ["application/json"])
    fun creditAccount(@PathVariable id: Long, @RequestBody amountRequest: AmountRequest): ResponseEntity<*> {
        return when (val outcome = accountService.credit(id, amountRequest.amount)) {
            is Success -> ResponseEntity.ok(AccountDTO(outcome.account.id!!, outcome.account.balance, outcome.account.owner))
            is AmountNotGreaterThanZero -> ResponseEntity(ErrorResponse("amount must be greater than zero"), HttpStatus.CONFLICT)
            is AccountNotFound -> ResponseEntity(ErrorResponse("Not found"), HttpStatus.NOT_FOUND)
            is Unauthorized -> ResponseEntity(ErrorResponse("Unauthorized"), HttpStatus.UNAUTHORIZED)
            else -> ResponseEntity(ErrorResponse("Unexpected outcome: ${outcome::class.simpleName}"), HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

}

data class AccountsDto(var accounts: List<AccountDTO>)

data class ErrorResponse(var message : String)

data class CreateAccountRequest(var initialBalance: Long)
data class AmountRequest(var amount: Long)

data class AccountDTO(var id: Long, var balance: Long, var owner: String)