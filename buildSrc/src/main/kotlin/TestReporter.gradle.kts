gradle.projectsEvaluated {

// Share the test report data to be aggregated for the whole project
// https://docs.gradle.org/current/userguide/java_testing.html#test_reporting

    configurations.create("binaryTestResultsElements") {
        isCanBeResolved = false
        isCanBeConsumed = true
        attributes {
            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
            attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named("test-report-data"))
        }
        listOf("test", "integrationTest", "componentTest").forEach { taskName ->
            val task = tasks.findByName(taskName)
            if (task != null)
                outgoing.artifact((task as Test).binaryResultsDirectory.get() )
        }
    }

}