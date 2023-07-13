package org.sonar.iac.arm.tree.api.bicep.variable;

import org.sonar.iac.arm.tree.api.ArmTree;

/**
 * Lambda function:
 * `lambdaVariable> => expression`
 * where
 * `lambdaVariable = ( variableBlock | localVariable )`
 */
public interface LambdaVariable extends ArmTree {
}
