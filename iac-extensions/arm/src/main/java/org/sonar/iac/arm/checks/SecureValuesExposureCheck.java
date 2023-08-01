package org.sonar.iac.arm.checks;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualProperty;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.ArmTreeUtils;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.impl.bicep.ParameterDeclarationImpl;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checkdsl.ContextualTree;
import org.sonar.iac.common.checks.TextUtils;

import static org.sonar.iac.arm.tree.ArmTreeUtils.containsParameterReference;

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
    Map<String, ParameterDeclaration> sensitiveParameters = ArmTreeUtils.getParametersByNames(file)
      .entrySet()
      .stream()
      .filter(it -> it.getValue().type() == ParameterType.SECURE_OBJECT || it.getValue().type() == ParameterType.SECURE_STRING || isSecureParameterInBicep(it.getValue()))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    if (sensitiveParameters.isEmpty()) {
      return;
    }

    ContextualObject expressionEvaluationOptions = resource.object("expressionEvaluationOptions");
    ContextualProperty scope = expressionEvaluationOptions.property("scope");
    if (scope.isAbsent() || TextUtils.isValue(scope.valueOrNull(), "Inner").isFalse()) {
      Set<String> sensitiveParameterNames = sensitiveParameters.keySet();
      List<SecondaryLocation> sensitiveParameterUsages = resource.object("template").list("resources").objects()
        .filter(ContextualTree::isPresent)
        .flatMap(o -> o.tree.allPropertiesFlattened())
        .map(Property::value)
        .filter(containsParameterReference(sensitiveParameterNames))
        .map(value -> new SecondaryLocation(value.textRange(), SECONDARY_MESSAGE))
        .collect(Collectors.toList());

      // TODO: also check nested templates

      if (!sensitiveParameterUsages.isEmpty()) {
        scope.report(MESSAGE, sensitiveParameterUsages)
          .reportIfAbsent(MESSAGE, sensitiveParameterUsages);
        expressionEvaluationOptions.reportIfAbsent(MESSAGE, sensitiveParameterUsages);
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
