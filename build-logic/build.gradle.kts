plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(libs.jfrog.buildinfo.gradle)
  implementation(libs.sonar.scanner.gradle)
  implementation(kotlin("stdlib"))
  implementation(libs.diffplug.spotless)
  implementation(libs.diffplug.blowdryer)
}
