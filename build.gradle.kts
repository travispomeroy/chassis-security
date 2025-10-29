buildscript {
    extra.apply {
        set("kotlin_version", "1.4.20")
    }

    repositories {
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10") // JVM
        classpath("org.jetbrains.kotlin:kotlin-noarg:1.6.10")   // JPA
        classpath("org.jetbrains.kotlin:kotlin-allopen:1.6.10") // Spring/All open
        classpath("com.avast.gradle:gradle-docker-compose-plugin:$dockerComposePluginVersion")
    }
}

plugins {
    id("org.springframework.boot") version springBootVersion apply false
    id("io.spring.dependency-management") version "1.0.10.RELEASE" apply false
    kotlin("plugin.allopen") version "1.6.10"
    kotlin("jvm") version "1.6.10"
}

val stopContainersDefault : String by project

apply(plugin = "docker-compose")

configure<com.avast.gradle.dockercompose.ComposeExtension> {
    val pn : String? = null
    setProjectName(pn)
    removeContainers.set(false)
    stopContainers.set(stopContainersDefault.toBoolean())
    buildBeforePull.set(false)
    includeDependencies.set(true)

    createNested("sql").apply {
        setProjectName(pn)
        startedServices.set(listOf("mysql"))
    }

    createNested("prometheus").apply {
        setProjectName(pn)
        startedServices.set(listOf("mysql", "prometheus"))
    }

    createNested("zipkin").apply {
        setProjectName(pn)
        startedServices.set(listOf("mysql", "prometheus", "zipkin"))
    }

    createNested("keycloak").apply {
        setProjectName(pn)
        startedServices.set(listOf("keycloak"))
    }
    createNested("sqlAndKeycloak").apply {
        setProjectName(pn)
        startedServices.set(listOf("mysql", "keycloak"))
    }
}

gradle.projectsEvaluated {
    val assemble = tasks.findByPath(":service-template-main:assemble")
    for (task in tasks) {
        if (task.name.endsWith("composeUp", ignoreCase = true)) task.dependsOn(assemble)
    }
}

repositories {

    mavenCentral()
}


allprojects {

    group = "net.chrisrichardson.liveprojects.servicetemplate"
    version = "0.0.1-SNAPSHOT"


}


subprojects {

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    repositories {
        mavenCentral()
        maven(url = "https://repo.spring.io/milestone")
    }


    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.jetbrains.kotlin.plugin.allopen")



    configure<JavaPluginExtension> {

        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }

    }


    dependencies {

        implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

        // :

        constraints {
            implementation("com.google.guava:guava") {
                version {
                    strictly("25.0-jre")
                }
                because("Selenium conflict")
            }
        }

        implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.20")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.20")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

}

tasks.register("validateEnvironment", DefaultTask::class) {
    dependsOn("composePull")
}

val testReportData by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named("test-report-data"))
    }
}

val liveProject : String by project

val todoModules =
        mapOf(
                "series-01-live-project-01-configuring-observability" to listOf("service-template-health-check", "service-template-distributed-tracing", "service-template-metrics")
                ,"series-01-live-project-02-implementing-security" to listOf("service-template-web-security", "service-template-main")
        )

dependencies {
    todoModules[liveProject]?.forEach { testReportData(project(":$it")) }
}

val todoBuildTask = tasks.register("todoBuild", GradleBuild::class) {
    tasks = todoModules[liveProject]?.map { "${it}:check" } ?: listOf()
    startParameter.projectProperties.put("ignoreTestFailures", "true")
}.get()

tasks.register<TestReport>("todo") {
    destinationDir = file("$buildDir/reports/todoTestReport")
    (getTestResultDirs() as ConfigurableFileCollection).from(testReportData)
    dependsOn(todoBuildTask)
    doLast {
        print("\n\nSee ${destinationDir.toURI()}/index.html\n\n")
    }
}


val compileAllTask = tasks.register("compileAll", DefaultTask::class).get()

gradle.projectsEvaluated {

    project.subprojects.forEach { p ->
        p.tasks.forEach { t ->
            if (t.name.contains("testClasses", ignoreCase = true)) {
                compileAllTask.dependsOn(t)
            }
        }
    }
}

tasks.findByPath(":service-template-main:bootRun")!!.dependsOn(":sqlComposeUp")
