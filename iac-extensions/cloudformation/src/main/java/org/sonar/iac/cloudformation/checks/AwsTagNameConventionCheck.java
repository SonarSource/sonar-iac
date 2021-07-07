/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.checks.utils.XPathUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;

@Rule(key = "S6273")
public class AwsTagNameConventionCheck extends AbstractResourceCheck {
  protected static final String MESSAGE = "Rename tag key \"%s\" to match the regular expression \"%s\".";
  public static final String DEFAULT = "^([A-Z][A-Za-z]*:)*([A-Z][A-Za-z]*)$";
  protected Pattern pattern;

  @RuleProperty(
    key = "format",
    description = "Regular expression used to check the tag keys against.",
    defaultValue = DEFAULT)
  public String format = DEFAULT;

  @Override
  public void initialize(InitContext init) {
    pattern = Pattern.compile(format);
    super.initialize(init);
  }

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    getTagKeyStream(resource)
      .filter(this::isMismatchingKey)
      .forEach(key -> ctx.reportIssue(key, String.format(MESSAGE, key.value(), format)));
  }

  private static Stream<ScalarTree> getTagKeyStream(Resource resource) {
    return XPathUtils.getTrees(resource.properties(), "/Tags[]/Key").stream()
      .filter(ScalarTree.class::isInstance).map(ScalarTree.class::cast);
  }

  private boolean isMismatchingKey(ScalarTree tagKey) {
    return !pattern.matcher(tagKey.value()).matches();
  }
}
