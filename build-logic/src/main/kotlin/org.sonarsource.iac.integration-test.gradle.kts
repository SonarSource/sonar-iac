// Inspiration: https://docs.gradle.org/current/samples/sample_jvm_multi_project_with_additional_test_types.html

plugins {
    java
    id("org.sonarsource.iac.java-conventions")
}

val integrationTest by sourceSets.creating

configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

val integrationTestTask =
    tasks.register<Test>("integrationTest") {
        description = "Runs integration tests."
        group = "verification"
        useJUnitPlatform()

        testClassesDirs = integrationTest.output.classesDirs
        classpath = configurations[integrationTest.runtimeClasspathConfigurationName] + integrationTest.output

        testLogging {
            exceptionFormat =
                org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL // log the full stack trace (default is the 1st line of the stack trace)
            events("skipped", "failed") // verbose log for failed and skipped tests (by default the name of the tests are not logged)
        }
    }
