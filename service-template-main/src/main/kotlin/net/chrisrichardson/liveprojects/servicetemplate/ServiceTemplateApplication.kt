package net.chrisrichardson.liveprojects.servicetemplate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LiveProjectsSkeletonApplication

fun main(args: Array<String>) {
	runApplication<LiveProjectsSkeletonApplication>(*args)
}
