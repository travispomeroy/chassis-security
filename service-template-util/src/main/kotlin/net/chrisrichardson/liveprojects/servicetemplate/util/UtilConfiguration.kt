package net.chrisrichardson.liveprojects.servicetemplate.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class UtilConfiguration {

    @Primary
    @Bean
    fun objectMapper(): ObjectMapper {
        val om = ObjectMapper()
        om.registerModule(KotlinModule.Builder().build())
        // TODO - configuration here
        return om
    }

}