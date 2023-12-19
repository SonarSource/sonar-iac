plugins {
    id("org.sonarsource.iac.java-conventions")
}

description = "SonarSource IaC Analyzer :: Sonar Helm for IaC"

val sonar_sources by extra { "." }
val sonar_inclusions by extra { "**/*.go" }
val sonar_exclusions by extra { "**/build/**,**/org.sonarsource.iac.helm/**" }
val sonar_tests by extra { "." }
val sonar_test_inclusions by extra { "**/*_test.go" }
val sonar_go_tests_reportPaths by extra { "build/test-report.out" }
val sonar_go_coverage_reportPaths by extra { "build/test-coverage.out" }

tasks.register<Exec>("compileProtobufGo") {
  commandLine("protoc", "-I=${project.projectDir}", "-I=" + System.getProperty("user.home") + "/go/protobuf/include",
    "--go_out=${project.projectDir}", "${project.projectDir}/template-evaluation.proto")
  doFirst {
    println("Running command: ${commandLine}")
  }
  doLast {
    println("Compile protobuf done")
  }
}

// Define and trigger tasks in this order: clean, compile and test go code
tasks.register<Exec>("cleanGoCode") {
    callMake(this, "clean")
    doLast {
        println("cleanGoCode")
    }
}

tasks.register<Exec>("compileGoCode") {
    dependsOn("cleanGoCode")
    callMake(this, "build")
    doLast {
        println("compileGoCode")
    }
}

tasks.register<Exec>("testGoCode") {
  dependsOn("compileGoCode")
  callMake(this, "test")
  doLast {
    println("testGoCode")
  }
}

tasks.named("build") {
  dependsOn("testGoCode")
}

fun callMake(execTask: Exec, arg:String) {
  if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
    execTask.commandLine("cmd", "/c", "make.bat", arg)
  } else {
    execTask.commandLine("sh", "./make.sh", arg)
  }
}
