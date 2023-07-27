package org.sonar.iac.arm.checks;

import java.util.List;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualProperty;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.ArmTreeUtils;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.arm.tree.impl.bicep.ParameterDeclarationImpl;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6656")
public class SecureValuesExposureCheck extends AbstractArmResourceCheck {
  private static final String MESSAGE = "Change this code to not use an outer expression evaluation scope in nested templates.";
  private static final String SECONDARY_MESSAGE = "This secure parameter is leaked through the deployment history.";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Resources/deployments", SecureValuesExposureCheck::checkSecureParametersInNestedTemplates);
  }

  private static void checkSecureParametersInNestedTemplates(ContextualResource resource) {
    // TODO: after SONARIAC-1034 use symbol table instead of accessing parameters through `FILE`
    File file = ArmTreeUtils.getRootNode(resource.tree);
    List<ParameterDeclaration> sensitiveParameters = file.statements().stream()
      .filter(ParameterDeclaration.class::isInstance)
      .map(ParameterDeclaration.class::cast)
      .filter(it -> it.type() == ParameterType.SECURE_OBJECT || it.type() == ParameterType.SECURE_STRING || isSecureParameterInBicep(it))
      .collect(Collectors.toList());

    if (sensitiveParameters.isEmpty()) {
      return;
    }

    ContextualObject expressionEvaluationOptions = resource.object("expressionEvaluationOptions");
    ContextualProperty scope = expressionEvaluationOptions.property("scope");
    if (expressionEvaluationOptions.isAbsent() || scope.isAbsent() || TextUtils.isValue(scope.valueOrNull(), "Inner").isFalse()) {
      List<String> sensitiveParameterNames = sensitiveParameters.stream().map(it -> it.identifier().value()).collect(Collectors.toList());
      List<SecondaryLocation> sensitiveParameterUsages = resource.object("template").list("resources").objects()
        .flatMap(o -> o.tree.allLiteralProperties())
        .filter(prop -> sensitiveParameterNames.stream().anyMatch(p -> TextUtils.matchesValue(prop.value(), s -> s.contains(p)).isTrue()))
        .map(p -> new SecondaryLocation(p.textRange(), SECONDARY_MESSAGE))
        .collect(Collectors.toList());
      if (!sensitiveParameterUsages.isEmpty()) {
        scope.report(MESSAGE, sensitiveParameterUsages);
      }
    }
  }

  private static boolean isSecureParameterInBicep(ParameterDeclaration parameterDeclaration) {
    if (parameterDeclaration instanceof ParameterDeclarationImpl) {
      return ((ParameterDeclarationImpl) parameterDeclaration).findDecoratorByName("secure").isPresent();
    } else {
      return false;
    }
  }
}
