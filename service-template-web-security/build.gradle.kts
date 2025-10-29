plugins {
    id("TestReporter")
}


apply<IntegrationTestsPlugin>()
apply<RestAssuredTestDependenciesPlugin>()

dependencies {

    implementation(project(":service-template-util"))
    implementation(project(":service-template-domain"))


    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")

    testImplementation(project(":service-template-web"))
    testImplementation(project(":service-template-health-check"))
    testImplementation(project(":service-template-metrics"))

    testImplementation(project(":service-template-config"))
    testImplementation(project(":service-template-test-data"))
    testImplementation(project(":service-template-test-containers"))
    testImplementation(project(":service-template-test-keycloak"))
    testImplementation(project(":service-template-test-util"))

    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0") {

    }

}


tasks.findByName("check")!!.shouldRunAfter(":service-template-domain:check")
