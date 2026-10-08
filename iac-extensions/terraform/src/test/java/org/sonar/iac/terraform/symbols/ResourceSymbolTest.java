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
package org.sonar.iac.terraform.symbols;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.terraform.api.tree.BlockTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceSymbolTest extends AbstractSymbolTest {

  @Test
  void report() {
    BlockTree tree = parseBlock("resource \"my_type\" \"my_name\" {}");
    ResourceSymbol resource = ResourceSymbol.fromPresent(ctx, tree);
    resource.report("message");
    assertIssueReported(tree.labels().get(0), "message");
  }

  @Test
  void reportIfAbsent() {
    BlockTree tree = parseBlock("resource \"my_type\" \"my_name\" {}");
    ResourceSymbol resource = ResourceSymbol.fromPresent(ctx, tree);
    assertThatThrownBy(() -> resource.reportIfAbsent("message"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessage("Resource symbols should always exists");
  }

  @Test
  void reportIfAbsentWithResourceAsParent() {
    BlockTree resourceTree = parseBlock("resource \"my_type\" \"my_name\" {}");
    BlockSymbol resource = ResourceSymbol.fromPresent(ctx, resourceTree);
    BlockSymbol child = BlockSymbol.fromAbsent(ctx, "missing_block", resource);
    child.reportIfAbsent("%s");
    assertIssueReported(resourceTree.labels().get(0), "missing_block");
  }

  @Test
  void createIncompleteResource() {
    BlockTree tree = parseBlock("resource {}");
    ResourceSymbol resource = ResourceSymbol.fromPresent(ctx, tree);
    assertThat(resource.type).isEmpty();
    resource.report("message", Collections.emptyList());
    assertNoIssueReported();
  }

  @Test
  void writeOnlyCounterparts() {
    ResourceSymbol db = ResourceSymbol.fromPresent(ctx, parseBlock("resource \"aws_db_instance\" \"db\" { password_wo = \"secret\" }"));
    assertThat(db.writeOnlyCounterpart("password")).hasValue("password_wo");
    assertThat(db.writeOnlyCounterpart("username")).isEmpty();

    ResourceSymbol custom = ResourceSymbol.fromPresent(ctx, parseBlock("resource \"custom_resource\" \"x\" { token_wo = \"secret\" }"));
    assertThat(custom.writeOnlyCounterpart("token")).hasValue("token_wo");
    assertThat(custom.writeOnlyCounterpart("password")).isEmpty();

    ResourceSymbol data = ResourceSymbol.fromPresent(ctx, parseBlock("data \"aws_db_instance\" \"db\" {}"));
    assertThat(data.writeOnlyCounterpart("password")).isEmpty();
  }

  @Test
  void ephemeralBlocksDoNotHavePersistedResourceSemantics() {
    ResourceSymbol db = ResourceSymbol.fromPresent(ctx, parseBlock("""
      ephemeral "aws_db_instance" "db" {
        password = "secret"
        password_wo = "secret"
        password_wo_version = 1
      }
      """));
    assertThat(db.writeOnlyCounterpart("password")).isEmpty();
    assertThat(db.attribute("password_wo").isWriteOnly()).isFalse();
    assertThat(db.attribute("password_wo_version").isWriteOnlyVersion()).isFalse();

    ResourceSymbol data = ResourceSymbol.fromPresent(ctx, parseBlock("""
      ephemeral "terraform_data" "credentials" {
        store {}
      }
      """));
    assertThat(data.block("store").isSensitiveStore()).isFalse();
  }

  @ParameterizedTest
  @CsvSource({
    "aws_db_instance, password",
    "aws_docdb_cluster, master_password",
    "aws_rds_cluster, master_password",
    "aws_redshift_cluster, master_password",
    "aws_secretsmanager_secret_version, secret_string",
    "aws_ssm_parameter, value",
    "azurerm_key_vault_secret, value",
    "google_secret_manager_secret_version, secret_data",
    "google_sql_user, password"
  })
  void knownWriteOnlyCounterpartsDoNotRequireTheWriteOnlyArgumentInTheDeclaration(String resourceType, String persistedArgument) {
    String code = "resource \"" + resourceType + "\" \"x\" { " + persistedArgument + " = \"secret\" }";
    ResourceSymbol resource = ResourceSymbol.fromPresent(ctx, parseBlock(code));

    assertThat(resource.writeOnlyCounterpart(persistedArgument)).hasValue(persistedArgument + "_wo");
  }
}
