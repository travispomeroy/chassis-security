import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*

class IntegrationTestsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val sourceSets = project.the<SourceSetContainer>()
        val main = sourceSets.findByName("main")!!
        val test = sourceSets.findByName("test")!!


        val integrationTestSourceSet = sourceSets.create("integrationTest") {

            compileClasspath += main.output + test.output
            runtimeClasspath += main.output + test.output
        }

        val configurations = project.configurations

        configurations["integrationTestImplementation"].extendsFrom(configurations.findByName("testImplementation"))
        configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.findByName("runtimeOnly"))

        val integrationTest = project.task<Test>("integrationTest") {
            description = "Runs integration tests."
            group = "verification"

            testClassesDirs = integrationTestSourceSet.output.classesDirs
            classpath = integrationTestSourceSet.runtimeClasspath

            ignoreFailures = if (project.hasProperty("ignoreTestFailures")) (project.property("ignoreTestFailures") as String) == "true" else false

            shouldRunAfter("test")
        }

        project.tasks.findByName("check")!!.dependsOn(integrationTest)

    }
}