/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import com.sonar.sslr.api.typed.ActionParser;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.HclParser;
import org.sonar.sslr.grammar.GrammarRuleKey;

public abstract class TerraformTreeModelTest {
  protected ActionParser<TerraformTree> p;

  /**
   * Parse the given string and return the first descendant of the given kind.
   *
   * @param s the string to parse
   * @param rootRule the rule to start parsing from
   * @return the node found for the given kind, null if not found.
   */
  protected <T extends TerraformTree> T parse(String s, GrammarRuleKey rootRule) {
    p = new HclParser(rootRule);
    TerraformTree node = p.parse(s);

    //TODO: SONARIAC-91 similar to PHP parser, check how far we have parsed. Missing possibility to represent trees as string for now
    //checkFullFidelity(node, s.trim());

    return (T) node;
  }
}
