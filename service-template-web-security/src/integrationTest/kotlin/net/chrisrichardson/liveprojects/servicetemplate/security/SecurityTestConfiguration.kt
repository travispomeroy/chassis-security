package net.chrisrichardson.liveprojects.servicetemplate.security

import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountRepository
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountServiceImpl
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountServiceObserver
import net.chrisrichardson.liveprojects.servicetemplate.domain.AuthenticatedUserSupplier
import net.chrisrichardson.liveprojects.servicetemplate.util.UtilConfiguration
import net.chrisrichardson.liveprojects.servicetemplate.web.AccountController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@EnableAutoConfiguration
@Import(value = [UtilConfiguration::class])
@ComponentScan(basePackageClasses = [SecurityTestConfiguration::class, AccountController::class, ])
class SecurityTestConfiguration {

    @Autowired(required = false)
    var authenticatedUserSupplier: AuthenticatedUserSupplier  = AuthenticatedUserSupplier.EMPTY_SUPPLIER

    @Bean
    fun accountService(accountRepository: AccountRepository,
                       accountServiceObserver: AccountServiceObserver)  = AccountServiceImpl(accountRepository, authenticatedUserSupplier, accountServiceObserver)
}