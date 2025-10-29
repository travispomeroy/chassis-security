package net.chrisrichardson.liveprojects.servicetemplate.metrics

import io.micrometer.core.instrument.MeterRegistry
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountServiceObserver
import org.springframework.stereotype.Component

@Component
class MicrometerBasedAccountServiceObserver(meterRegistry: MeterRegistry) : AccountServiceObserver {

    val createCounter = meterRegistry.counter("account.created")

    val successfulDebitCounter = meterRegistry.counter("account.debit.success")

    val failedDebitCounter = meterRegistry.counter("account.debit.failure")

    val successfulCreditCounter = meterRegistry.counter("account.credit.success")

    val failedCreditCounter = meterRegistry.counter("account.credit.failure")

    val unauthorizedCounter = meterRegistry.counter("account.unauthorized")



    override fun noteAccountCreated() = createCounter.increment()


    override fun noteSuccessfulDebit() {
        successfulDebitCounter.increment()
    }

    override fun noteFailedDebit() {
        failedDebitCounter.increment()
    }

    override fun noteFailedCredit() {
        failedCreditCounter.increment()
    }

    override fun noteSuccessfulCredit() {
        successfulCreditCounter.increment()
    }

    override fun noteUnauthorizedAccountAccess() {
        unauthorizedCounter.increment()
    }

}

