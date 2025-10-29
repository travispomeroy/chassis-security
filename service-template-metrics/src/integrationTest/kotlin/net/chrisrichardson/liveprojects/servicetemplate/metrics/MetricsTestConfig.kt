package net.chrisrichardson.liveprojects.servicetemplate.metrics

import net.chrisrichardson.liveprojects.servicetemplate.util.UtilConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@EnableAutoConfiguration
@Import(value=[UtilConfiguration::class])
@ComponentScan
class MetricsTestConfig {


}