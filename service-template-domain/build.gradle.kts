

apply<IntegrationTestsPlugin>()

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.MappedSuperclass")
}

dependencies {

    api(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))

    api("org.springframework.data:spring-data-commons")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")

    testImplementation(project(":service-template-test-data"))

    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")



}

tasks.register("validateEnvironment", DefaultTask::class) {
    dependsOn("test")
    dependsOn("integrationTest")
}
