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
package org.sonar.iac.arm.checks;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.arm.checkdsl.ContextualProperty;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.checks.utils.CheckUtils;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S8679")
public class LogicAppConnectionUserAccountCheck extends AbstractArmResourceCheck {

  private static final String MESSAGE = "Use a service principal or managed identity instead of a user account for this API connection.";
  // Simplified E-mail regex.
  // O(n) in input length: possessive quantifiers and disjoint character classes prevent catastrophic backtracking.
  private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+\\-]*+(@(?:[a-zA-Z0-9\\-]++\\.)++[a-zA-Z]{2,}+)?");
  // Parameter names that, in Azure Logic Apps connection schemas, hold a user account identifier:
  // "username" is used by SQL, FileSystem, FTP, SFTP and similar connectors;
  // "authenticatedUser" is used by OAuth-based connectors (Office 365, SharePoint, Outlook, OneDrive, ...).
  private static final Set<String> SENSITIVE_PARAMETER_NAMES = Set.of("username", "authenticateduser");
  private static final String DEFAULT_ALLOWED_CONNECTORS = "office365,filesystem";

  @RuleProperty(
    key = "allowedConnectors",
    defaultValue = DEFAULT_ALLOWED_CONNECTORS,
    description = "Comma-separated list of connector API names exempt from this rule (connectors that inherently require user-delegated auth)")
  public String allowedConnectors = DEFAULT_ALLOWED_CONNECTORS;

  private Set<String> cachedAllowedConnectors;

  @Override
  protected void registerResourceConsumer() {
    initAllowedConnectorSet();
    register("Microsoft.Web/connections", this::checkConnection);
  }

  private void checkConnection(ContextualResource resource) {
    if (isAllowedConnector(resource)) {
      return;
    }

    if (resource.property("parameterValueType").is(expr -> TextUtils.matchesValue(expr, "Alternative"::equals).isTrue())) {
      return;
    }

    var parameterValues = resource.object("parameterValues");
    if (parameterValues.tree == null) {
      return;
    }

    for (var prop : parameterValues.tree.properties()) {
      if (!isSensitiveParameter(prop)) {
        continue;
      }
      TextUtils.getValue(prop.value()).ifPresent(value -> {
        if (containsEmailAddress(value)) {
          parameterValues.ctx.reportIssue(prop, MESSAGE);
        }
      });
    }
  }

  private static boolean isSensitiveParameter(PropertyTree prop) {
    return TextUtils.getValue(prop.key())
      .map(name -> SENSITIVE_PARAMETER_NAMES.contains(name.toLowerCase(Locale.ROOT)))
      .orElse(false);
  }

  private void initAllowedConnectorSet() {
    if (cachedAllowedConnectors == null) {
      cachedAllowedConnectors = Arrays.stream(allowedConnectors.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toUnmodifiableSet());
    }
  }

  private static boolean containsEmailAddress(String value) {
    var matcher = EMAIL_PATTERN.matcher(value);
    while (matcher.find()) {
      if (matcher.group(1) != null) {
        return true;
      }
    }
    return false;
  }

  private boolean isAllowedConnector(ContextualResource resource) {
    ContextualProperty apiId = resource.object("api").property("id");

    // For ARM/Bicep expressions (function calls), check if the expression tree contains any allowed connector name
    return cachedAllowedConnectors.stream().anyMatch(connectorName -> apiId.is(CheckUtils.containsRecursively(connectorName)));
  }
}
