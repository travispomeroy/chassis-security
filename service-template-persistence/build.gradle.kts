apply<IntegrationTestsPlugin>()

dependencies {

    implementation(project(":service-template-domain"))
    implementation(project(":service-template-config"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    runtimeOnly("mysql:mysql-connector-java:8.0.21")

    testImplementation(project(":service-template-test-data"))
    testImplementation(project(":service-template-test-containers"))


}

tasks.findByName("check")!!.shouldRunAfter(":service-template-domain:check")

tasks.register("validateEnvironment", DefaultTask::class) {
    dependsOn("integrationTest")
}


