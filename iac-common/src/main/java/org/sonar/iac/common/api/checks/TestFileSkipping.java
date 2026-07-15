/*
 * SonarQube IaC Plugin
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
package org.sonar.iac.common.api.checks;

/**
 * Marker interface for {@link IacCheck} implementations that should not raise issues on test files.
 * Checks implementing this interface are automatically skipped by {@link org.sonar.iac.common.extension.visitors.ChecksVisitor}
 * when a {@link org.sonarsource.analyzer.commons.appsec.TestFileClassifier} is supplied and classifies the current file as a test file.
 */
public interface TestFileSkipping {
}
