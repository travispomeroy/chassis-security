package net.chrisrichardson.liveprojects.servicetemplate.domain

import org.springframework.data.repository.CrudRepository
import java.util.*

interface AccountRepository : CrudRepository<Account, Long> {
    fun findByOwner(owner: String): Iterable<Account>
}