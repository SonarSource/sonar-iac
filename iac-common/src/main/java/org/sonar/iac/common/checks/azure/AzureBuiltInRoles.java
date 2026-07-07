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
package org.sonar.iac.common.checks.azure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * The catalog of Azure built-in role definitions, mapping each role's {@code roleDefinitionId} GUID to its
 * human-readable name and back. The data is loaded once from {@code azure-builtin-roles.csv} (a
 * {@code roleDefinitionId,roleName} table with a header row), generated from the
 * <a href="https://learn.microsoft.com/en-us/azure/role-based-access-control/built-in-roles">official Microsoft list</a>,
 * whose source is the summary table in
 * <a href="https://github.com/MicrosoftDocs/azure-docs/blob/main/articles/role-based-access-control/built-in-roles.md">MicrosoftDocs/azure-docs built-in-roles.md</a>.
 * There is no automated regeneration; refresh the CSV manually from that table when Azure adds roles.
 * <p>
 * Used by the S6387 telemetry (SONARIAC-2897): a role name / id is only reported for a known built-in role; custom
 * roles carry customer-specific identifiers and are reported as {@link #CUSTOM} instead.
 */
public final class AzureBuiltInRoles {

  /** Placeholder emitted for any role that is not a known built-in role, to avoid leaking custom role identifiers. */
  public static final String CUSTOM = "custom";

  private static final String RESOURCE_PATH = "/org/sonar/iac/common/checks/azure/azure-builtin-roles.csv";
  private static final Pattern GUID = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

  private static final Map<String, String> ID_TO_NAME = new HashMap<>();
  // Keyed by lower-cased name because Azure treats role names case-insensitively.
  private static final Map<String, String> LOWER_NAME_TO_ID = new HashMap<>();

  static {
    load();
  }

  private AzureBuiltInRoles() {
  }

  private static void load() {
    try (InputStream input = AzureBuiltInRoles.class.getResourceAsStream(RESOURCE_PATH)) {
      if (input == null) {
        throw new IllegalStateException("Missing resource: " + RESOURCE_PATH);
      }
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
        // Skip the CSV header row (roleDefinitionId,roleName).
        reader.readLine();
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.isBlank()) {
            continue;
          }
          // GUIDs never contain a comma, so the first comma always separates the id from the (unquoted) role name.
          int separator = line.indexOf(',');
          String id = line.substring(0, separator).toLowerCase(Locale.ROOT);
          String name = line.substring(separator + 1);
          ID_TO_NAME.put(id, name);
          LOWER_NAME_TO_ID.put(name.toLowerCase(Locale.ROOT), id);
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to load Azure built-in role definitions", e);
    }
  }

  /**
   * Extracts the trailing role definition GUID from a raw {@code roleDefinitionId}, which may be a bare GUID or a full
   * resource path such as {@code /subscriptions/.../roleDefinitions/<guid>}, and normalizes it to lower case.
   */
  public static Optional<String> normalizeId(@Nullable String rawRoleDefinitionId) {
    if (rawRoleDefinitionId == null) {
      return Optional.empty();
    }
    var matcher = GUID.matcher(rawRoleDefinitionId);
    String lastMatch = null;
    while (matcher.find()) {
      lastMatch = matcher.group();
    }
    return Optional.ofNullable(lastMatch).map(guid -> guid.toLowerCase(Locale.ROOT));
  }

  public static boolean isBuiltInId(String roleDefinitionId) {
    return ID_TO_NAME.containsKey(roleDefinitionId.toLowerCase(Locale.ROOT));
  }

  public static boolean isBuiltInName(String roleName) {
    return LOWER_NAME_TO_ID.containsKey(roleName.toLowerCase(Locale.ROOT));
  }

  public static Optional<String> nameForId(String roleDefinitionId) {
    return Optional.ofNullable(ID_TO_NAME.get(roleDefinitionId.toLowerCase(Locale.ROOT)));
  }

  public static Optional<String> idForName(String roleName) {
    return Optional.ofNullable(LOWER_NAME_TO_ID.get(roleName.toLowerCase(Locale.ROOT)));
  }
}
