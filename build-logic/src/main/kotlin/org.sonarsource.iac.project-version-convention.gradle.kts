plugins {
}

// this value is present on CI
val buildNumber: String? = System.getProperty("buildNumber")
project.ext["buildNumber"] = buildNumber
if (project.version.toString().endsWith("-SNAPSHOT") && buildNumber != null) {
    val versionSuffix = if (project.version.toString().count { it == '.' } == 1) ".0.$buildNumber" else ".$buildNumber"
    project.version = project.version.toString().replace("-SNAPSHOT", versionSuffix)
    logger.lifecycle("Project version set to $version")
}
