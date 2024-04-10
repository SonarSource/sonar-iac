package org.sonar.iac.arm.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Rule(key = "S1192")
public class StringLiteralDuplicatedCheck implements IacCheck {

  private static final String MESSAGE = "Define a constant instead of duplicating this literal \"%s\" %s times.";
  private static final String SECONDARY_MESSAGE = "Duplication.";
  public static final int THRESHOLD_DEFAULT = 3;
  public static final int MINIMAL_LITERAL_LENGTH_DEFAULT = 5;

  @RuleProperty(
    key = "threshold",
    defaultValue = "" + THRESHOLD_DEFAULT)
  int threshold = THRESHOLD_DEFAULT;

  @RuleProperty(
    key = "minimal_literal_length",
    defaultValue = "" + MINIMAL_LITERAL_LENGTH_DEFAULT)
  int minimalLiteralLength = MINIMAL_LITERAL_LENGTH_DEFAULT;

  private Map<String, List<StringLiteral>> stringLiteralMap;

  @Override
  public void initialize(InitContext init) {
    init.register(File.class, this::initFile);
    init.register(StringLiteral.class, this::visitStringLiteral);
  }

  private void initFile(CheckContext ctx, File file) {
    stringLiteralMap = new HashMap<>();
  }

  private void visitStringLiteral(CheckContext ctx, StringLiteral stringLiteral) {
    String value = stringLiteral.value();
    if (canBeReplaced(stringLiteral)) {
      List<StringLiteral> stringLiteralList = stringLiteralMap.computeIfAbsent(value, key -> new ArrayList<>());
      stringLiteralList.add(stringLiteral);
      if (stringLiteralList.size() == threshold) {
        List<SecondaryLocation> secondaryLocations = stringLiteralList.stream()
          .skip(1)
          .map(str -> new SecondaryLocation(str, SECONDARY_MESSAGE))
          .toList();
        ctx.reportIssue(stringLiteralList.get(0), MESSAGE.formatted(value, threshold), secondaryLocations);
      }
    }
  }

  /**
   * Return true if the provided StringLiteral can be replaced by a variable, meaning that otherwise we should not report it as a duplicate.
   * For example, a {@link StringLiteral} that is the {@link ResourceDeclaration#type()} and {@link ResourceDeclaration#version()} cannot be replaced.
   */
  private boolean canBeReplaced(StringLiteral stringLiteral) {
    // If the parent is a ResourceDeclaration, then we consider the StringLiteral is the typeAndVersion field => cannot be replaced
    return stringLiteral.value().length() >= minimalLiteralLength && !isResourceTypeAndApiVersionField(stringLiteral);
  }

  private boolean isResourceTypeAndApiVersionField(StringLiteral stringLiteral) {
    Tree parent = stringLiteral.parent();
    if (parent instanceof ResourceDeclaration resource) {
      if (resource.type() == stringLiteral || resource.version() == stringLiteral) {
        return true;
      }
      if (resource.version() != null && stringLiteral.value().equals(resource.type().value() + "@" + resource.version().value())) {
        return true;
      }
    }
    return false;
  }
}
