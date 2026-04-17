/*
 * SonarSource Cloud Native Gradle Modules
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
plugins {}

configurations.matching { it.name == "kotlinBouncyCastleConfiguration" }.configureEach {
    // Workaround for https://github.com/gradle/gradle/issues/35309.
    // When any of cloud-native Gradle plugins is applied in a project
    // whose Gradle version embeds Kotlin <2.3.20, there will be an unnecessary dependency on build classpath.
    withDependencies { clear() }
}
