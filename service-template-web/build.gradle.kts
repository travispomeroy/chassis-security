import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply<IntegrationTestsPlugin>()
apply<RestAssuredTestDependenciesPlugin>()

dependencies {

    implementation(project(":service-template-util"))
    implementation(project(":service-template-domain"))

    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation(project(":service-template-test-data"))
    testImplementation(project(":service-template-test-containers"))

    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")

}

tasks.findByName("check")!!.shouldRunAfter(":service-template-domain:check")
