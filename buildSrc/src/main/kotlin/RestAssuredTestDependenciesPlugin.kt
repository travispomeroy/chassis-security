import org.gradle.api.Plugin
import org.gradle.api.Project


abstract class AbstractRestAssuredDependenciesPlugin {
    abstract val configurationName: String


    fun apply(project: Project) {

        fun add(artifact : String) {
            project.dependencies.add(configurationName, artifact)
        }

        add("io.rest-assured:rest-assured:$restAssuredVersion")
        add("io.rest-assured:json-path:$restAssuredVersion")
        add("io.rest-assured:spring-mock-mvc:$restAssuredVersion")

        // The following are needed to fix some dependency issues

        add("io.rest-assured:xml-path:$restAssuredVersion")
        add("io.rest-assured:json-schema-validator:$restAssuredVersion")

        add("org.codehaus.groovy:groovy:3.0.7")
        add("org.codehaus.groovy:groovy-xml:3.0.7")

    }
}

class RestAssuredTestDependenciesPlugin : Plugin<Project>, AbstractRestAssuredDependenciesPlugin() {

    override val configurationName = "testImplementation"

}

class RestAssuredDependenciesPlugin : Plugin<Project>, AbstractRestAssuredDependenciesPlugin() {

    override val configurationName = "implementation"

}

