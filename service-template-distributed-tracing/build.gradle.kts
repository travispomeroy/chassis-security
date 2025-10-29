plugins {
    id("TestReporter")
}

apply<IntegrationTestsPlugin>()
apply<RestAssuredTestDependenciesPlugin>()

dependencies {



    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.0"))


    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("org.springframework.cloud:spring-cloud-sleuth-zipkin") // :3.0.1

    testImplementation(project(":service-template-domain"))
    testImplementation(project(":service-template-web"))
    testImplementation(project(":service-template-util"))
    testImplementation(project(":service-template-test-util"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(project(":service-template-test-containers"))

}
