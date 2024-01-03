plugins {
    `java-library`
}

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

group = "org.sonarsource.iac"
version = "1.25-SNAPSHOT"

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    options {
        (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
//        exceptionFormat =
//            org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT // log the full stack trace (default is the 1st line of the stack trace)
        events("skipped", "failed")
    }
}
