plugins {
    id("TestReporter")
}

apply<IntegrationTestsPlugin>()
apply<RestAssuredTestDependenciesPlugin>()

dependencies {
    implementation(project(":service-template-domain"))
    implementation(project(":service-template-util"))


    implementation("io.micrometer:micrometer-registry-prometheus:1.6.4")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation(project(":service-template-test-util"))
    testImplementation(project(":service-template-config"))
    testImplementation(project(":service-template-test-data"))
    testImplementation(project(":service-template-test-containers"))
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")



}

tasks.findByName("check")!!.shouldRunAfter(":service-template-domain:check")
