



plugins {
    id("TestReporter")
    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

apply<IntegrationTestsPlugin>()


apply<RestAssuredTestDependenciesPlugin>()



dependencies {

    implementation(project(":service-template-util"))
    implementation(project(":service-template-domain"))
    implementation(project(":service-template-config"))
    implementation(project(":service-template-persistence"))
    implementation(project(":service-template-web"))
    implementation(project(":service-template-web-security"))
    implementation(project(":service-template-distributed-tracing"))

    implementation(project(":service-template-health-check"))
    implementation(project(":service-template-metrics"))

    implementation("org.springframework.boot:spring-boot-devtools")

    implementation("org.springdoc:springdoc-openapi-ui:1.6.6")

    testImplementation(project(":service-template-test-util"))
    testImplementation(project(":service-template-test-data"))
    testImplementation(project(":service-template-test-containers"))
    testImplementation(project(":service-template-test-keycloak"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")

    testImplementation("org.testcontainers:selenium:$testContainersVersion")
    testImplementation("org.seleniumhq.selenium:selenium-remote-driver:3.141.59")
    testImplementation("org.seleniumhq.selenium:selenium-chrome-driver:3.141.59")

}


val integrationTest = tasks.findByName("integrationTest")!! as Test

integrationTest.apply {
    this.excludes.add("**/SwaggerUITests*")
}

tasks.register("testSwaggerUI", Test::class) {
    this.includes.add("**/SwaggerUITests*")
    testClassesDirs = integrationTest.testClassesDirs
    classpath = integrationTest.classpath

}

val checkTask = tasks.findByName("check")!!

checkTask.shouldRunAfter(":service-template-domain:check")
checkTask.shouldRunAfter(":service-template-web:check")
checkTask.shouldRunAfter(":service-template-web-security:check")
checkTask.shouldRunAfter(":service-template-persistence:check")
checkTask.shouldRunAfter(":service-template-metrics:check")
checkTask.shouldRunAfter(":service-template-health-check:check")
checkTask.shouldRunAfter(":service-template-distributed-tracing:check")


tasks.register("validateEnvironment", Test::class) {
    filter {
        includeTestsMatching("*ServiceTemplateApplicationTests")
    }
    val test = tasks.findByName("integrationTest")!! as Test
    testClassesDirs = test.testClassesDirs
    classpath += sourceSets["integrationTest"].runtimeClasspath
}
