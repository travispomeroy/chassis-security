package net.chrisrichardson.liveprojects.servicetemplate.persistence

import net.chrisrichardson.liveprojects.servicetemplate.domain.Account
import net.chrisrichardson.liveprojects.servicetemplate.domain.AccountRepository
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EnableJpaRepositories(basePackageClasses = [AccountRepository::class])
@EntityScan(basePackageClasses = [Account::class])
class DomainPersistenceConfiguration