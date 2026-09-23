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

import org.junit.jupiter.api.Test;

import static org.sonar.iac.common.testing.Verifier.issue;

class EmptyOrNullValueCheckTest {
  private static final EmptyOrNullValueCheck CHECK = new EmptyOrNullValueCheck();

  @Test
  void testEmptyOrNullValueJson() {
    ArmVerifier.verify("EmptyOrNullValueCheckTest/emptyOrNullValue.json", CHECK,
      issue(6, 4, 6, 16, "Remove this null variable or complete with real code."),
      issue(7, 4, 7, 14, "Remove this empty string or complete with real code."),
      issue(8, 4, 8, 14, "Remove this empty object or complete with real code."),
      issue(9, 4, 9, 14, "Remove this empty array or complete with real code."),
      issue(21, 8, 21, 19, "Remove this null property or complete with real code."),
      issue(56, 6, 56, 19, "Remove this null property or complete with real code."),
      issue(57, 6, 57, 16, "Remove this empty string or complete with real code."),
      issue(58, 6, 58, 16, "Remove this empty object or complete with real code."),
      issue(59, 6, 59, 17, "Remove this empty array or complete with real code."),
      issue(61, 8, 61, 21),
      issue(62, 8, 62, 19),
      issue(63, 8, 63, 19),
      issue(64, 8, 64, 19),
      issue(80, 12, 80, 23),
      issue(85, 12, 85, 23),
      issue(92, 10, 92, 26),
      issue(93, 10, 93, 25),
      issue(94, 10, 94, 25),
      issue(95, 10, 95, 25),
      issue(119, 4, 121, 19),
      issue(123, 4, 125, 17),
      issue(127, 4, 129, 17),
      issue(131, 4, 133, 17),
      issue(162, 8, 162, 19),
      issue(173, 10, 173, 21)
    // TODO SONARIAC-1403 ARM Template parser should produce the same AST as Bicep for output with FOR loop
    // issue(170, 10, 170, 24),
    // issue(181, 10, 181, 22),
    // issue(182, 10, 182, 22),
    // issue(183, 10, 183, 22),
    // issue(193, 10, 183, 24),
    // issue(202, 15, 202, 26)
    );
  }

  @Test
  void shouldAllowExceptionsInJson() {
    ArmVerifier.verifyNoIssue("EmptyOrNullValueCheckTest/emptyOrNullValue-exceptions.json", CHECK);
  }

  @Test
  void shouldAllowEmptyLogicAppRunAfterJson() {
    ArmVerifier.verifyContent("""
      {
        "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#",
        "contentVersion": "1.0.0.0",
        "resources": [
          {
            "type": "Microsoft.Logic/workflows",
            "apiVersion": "2019-05-01",
            "name": "firstWorkflow",
            "location": "westeurope",
            "properties": {
              "definition": {
                "actions": {
                  "firstAction": {
                    "runAfter": {}
                  },
                  "scope": {
                    "type": "scope",
                    "actions": {
                      "nestedAction": {
                        "runAfter": {}
                      },
                      "nestedScope": {
                        "type": "Scope",
                        "actions": {
                          "deeplyNestedAction": {
                            "runAfter": {}
                          }
                        }
                      }
                    }
                  },
                  "condition": {
                    "type": "If",
                    "actions": {
                      "trueAction": {
                        "runAfter": {}
                      }
                    },
                    "else": {
                      "actions": {
                        "falseAction": {
                          "runAfter": {}
                        }
                      }
                    }
                  },
                  "forEach": {
                    "type": "foreach",
                    "actions": {
                      "loopAction": {
                        "runAfter": {}
                      }
                    }
                  },
                  "until": {
                    "type": "until",
                    "actions": {
                      "untilAction": {
                        "runAfter": {}
                      }
                    }
                  },
                  "switch": {
                    "type": "switch",
                    "cases": {
                      "case": {
                        "actions": {
                          "caseAction": {
                            "runAfter": {}
                          }
                        }
                      },
                      "default": {
                        "actions": {
                          "caseNamedDefaultAction": {
                            "runAfter": {}
                          }
                        }
                      }
                    },
                    "default": {
                      "actions": {
                        "defaultAction": {
                          "runAfter": {}
                        }
                      }
                    }
                  }
                }
              }
            }
          },
          {
            "type": "microsoft.logic/WORKFLOWS",
            "apiVersion": "2024-10-01",
            "name": "secondWorkflow",
            "location": "westeurope",
            "properties": {
              "definition": {
                "actions": {
                  "anotherAction": {
                    "runAfter": {}
                  }
                }
              }
            }
          }
        ]
      }
      """, CHECK);
  }

  @Test
  void shouldReportOtherEmptyLogicAppValuesJson() {
    ArmVerifier.verifyContent("""
      {
        "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#",
        "contentVersion": "1.0.0.0",
        "resources": [
          {
            "type": "Microsoft.Logic/workflows",
            "apiVersion": "2019-05-01",
            "name": "workflow",
            "location": "westeurope",
            "properties": {
              "definition": {
                "actions": {
                  "action": {
                    "inputs": {},
                    "metadata": {
                      "runAfter": {}
                    },
                    "runAfter": []
                  },
                  "actionWithInputData": {
                    "inputs": {
                      "actions": {
                        "payload": {
                          "runAfter": {}
                        }
                      }
                    }
                  }
                },
                "runAfter": {}
              }
            }
          },
          {
            "type": "Microsoft.Storage/storageAccounts",
            "apiVersion": "2023-01-01",
            "name": "storage",
            "location": "westeurope",
            "properties": {
              "definition": {
                "actions": {
                  "action": {
                    "runAfter": {}
                  }
                }
              }
            }
          }
        ]
      }
      """, CHECK,
      issue(14, 14, 14, 26, "Remove this empty object or complete with real code."),
      issue(16, 16, 16, 30, "Remove this empty object or complete with real code."),
      issue(18, 14, 18, 28, "Remove this empty array or complete with real code."),
      issue(24, 20, 24, 34, "Remove this empty object or complete with real code."),
      issue(30, 10, 30, 24, "Remove this empty object or complete with real code."),
      issue(43, 14, 43, 28, "Remove this empty object or complete with real code."));
  }

  /**
   * Verifies that matching empty defaults are allowed in nested deployment templates.
   */
  @Test
  void shouldAllowMatchingNestedTemplateParameterDefaults() {
    ArmVerifier.verifyContent("""
      {
        "resources": [
          {
            "type": "Microsoft.Resources/deployments",
            "apiVersion": "2022-09-01",
            "name": "nestedTemplate",
            "properties": {
              "template": {
                "parameters": {
                  "tags": {
                    "type": "object",
                    "defaultValue": {}
                  },
                  "ipRules": {
                    "type": "array",
                    "defaultValue": []
                  }
                }
              }
            }
          }
        ]
      }
      """, CHECK);

    BicepVerifier.verifyContentNoIssue("""
      resource nestedTemplate 'Microsoft.Resources/deployments@2022-09-01' = {
        name: 'nestedTemplate'
        properties: {
          template: {
            parameters: {
              tags: {
                type: 'object'
                defaultValue: {}
              }
              ipRules: {
                type: 'array'
                defaultValue: []
              }
            }
          }
        }
      }
      """, CHECK);
  }

  @Test
  void shouldAllowMatchingDefaultsInNestedTemplatesInsideNestedTemplates() {
    ArmVerifier.verifyContent("""
      {
        "resources": [
          {
            "type": "Microsoft.Resources/deployments",
            "apiVersion": "2022-09-01",
            "name": "outerTemplate",
            "properties": {
              "template": {
                "resources": [
                  {
                    "type": "Microsoft.Resources/deployments",
                    "apiVersion": "2022-09-01",
                    "name": "innerTemplate",
                    "properties": {
                      "template": {
                        "parameters": {
                          "tags": {
                            "type": "object",
                            "defaultValue": {}
                          },
                          "ipRules": {
                            "type": "array",
                            "defaultValue": []
                          }
                        }
                      }
                    }
                  }
                ]
              }
            }
          }
        ]
      }
      """, CHECK);
  }

  @Test
  void shouldReportDefaultsInsideParameterValues() {
    ArmVerifier.verifyContent("""
      {"resources":[{"type":"Microsoft.Resources/deployments","apiVersion":"2022-09-01","name":"deployment","properties":{
        "parameters":{"configuration":{"value":{"template":{"parameters":{"tags":{
          "type":"object",
          "defaultValue":{}
        }}}}}}
      }}]}
      """, CHECK, issue(4, 4, 4, 21, "Remove this empty object or complete with real code."));

    BicepVerifier.verifyContent("""
      resource deployment 'Microsoft.Resources/deployments@2022-09-01' = {
        name: 'deployment'
        properties: {
          parameters: {
            configuration: {
              value: {
                template: {
                  parameters: {
                    tags: {
                      type: 'object'
                      defaultValue: {}
                    }
                  }
                }
              }
            }
          }
        }
      }
      """, CHECK, issue(11, 16, 11, 32, "Remove this empty object or complete with real code."));
  }

  /**
   * Verifies that only matching nested template parameter defaults are allowed.
   */
  @Test
  void shouldReportOtherNestedTemplateEmptyValues() {
    ArmVerifier.verifyContent("""
      {
        "resources": [
          {
            "type": "Microsoft.Resources/deployments",
            "apiVersion": "2022-09-01",
            "name": "nestedTemplate",
            "properties": {
              "template": {
                "parameters": {
                  "objectWithArray": {
                    "type": "object",
                    "defaultValue": []
                  },
                  "arrayWithObject": {
                    "type": "array",
                    "defaultValue": {}
                  },
                  "missingType": {
                    "defaultValue": {}
                  },
                  "dynamicType": {
                    "type": "[variables('parameterType')]",
                    "defaultValue": []
                  },
                  "unknownType": {
                    "type": "custom",
                    "defaultValue": {}
                  },
                  "nullDefault": {
                    "type": "object",
                    "defaultValue": null
                  },
                  "stringDefault": {
                    "type": "string",
                    "defaultValue": ""
                  }
                },
                "metadata": {
                  "defaultValue": {}
                },
                "unexpected": []
              }
            }
          }
        ]
      }
      """, CHECK,
      issue(12, 14, 12, 32, "Remove this empty array or complete with real code."),
      issue(16, 14, 16, 32, "Remove this empty object or complete with real code."),
      issue(19, 14, 19, 32, "Remove this empty object or complete with real code."),
      issue(23, 14, 23, 32, "Remove this empty array or complete with real code."),
      issue(27, 14, 27, 32, "Remove this empty object or complete with real code."),
      issue(31, 14, 31, 34, "Remove this null property or complete with real code."),
      issue(35, 14, 35, 32, "Remove this empty string or complete with real code."),
      issue(39, 12, 39, 30, "Remove this empty object or complete with real code."),
      issue(41, 10, 41, 26, "Remove this empty array or complete with real code."));

    BicepVerifier.verifyContent("""
      resource nestedTemplate 'Microsoft.Resources/deployments@2022-09-01' = {
        name: 'nestedTemplate'
        properties: {
          template: {
            parameters: {
              objectWithArray: {
                type: 'object'
                defaultValue: []
              }
              arrayWithObject: {
                type: 'array'
                defaultValue: {}
              }
              missingType: {
                defaultValue: {}
              }
              dynamicType: {
                type: parameterType
                defaultValue: []
              }
              unknownType: {
                type: 'custom'
                defaultValue: {}
              }
              nullDefault: {
                type: 'object'
                defaultValue: null
              }
              stringDefault: {
                type: 'string'
                defaultValue: ''
              }
            }
            metadata: {
              defaultValue: {}
            }
            unexpected: []
          }
        }
      }
      """, CHECK,
      issue(8, 10, 8, 26, "Remove this empty array or complete with real code."),
      issue(12, 10, 12, 26, "Remove this empty object or complete with real code."),
      issue(15, 10, 15, 26, "Remove this empty object or complete with real code."),
      issue(19, 10, 19, 26, "Remove this empty array or complete with real code."),
      issue(23, 10, 23, 26, "Remove this empty object or complete with real code."),
      issue(27, 10, 27, 28, "Remove this null property or complete with real code."),
      issue(31, 10, 31, 26, "Remove this empty string or complete with real code."),
      issue(35, 8, 35, 24, "Remove this empty object or complete with real code."),
      issue(37, 6, 37, 20, "Remove this empty array or complete with real code."));
  }

  @Test
  void shouldCheckEmptyValuesInVariableCopyDirectiveInput() {
    ArmVerifier.verifyContent("""
      {
        "variables": {
          "copy": [
            {
              "name": "generatedVariable",
              "count": 1,
              "input": {
                "unfinished": ""
              }
            }
          ]
        }
      }
      """, CHECK,
      issue(8, 10, 8, 26, "Remove this empty string or complete with real code."));
  }

  @Test
  void testEmptyOrNullValueBicep() {
    BicepVerifier.verify("EmptyOrNullValueCheckTest/emptyOrNullValue.bicep", CHECK);
  }

  @Test
  void shouldAllowEmptyLogicAppRunAfterBicep() {
    BicepVerifier.verifyContentNoIssue("""
      resource firstWorkflow 'Microsoft.Logic/workflows' = {
        name: 'firstWorkflow'
        location: 'westeurope'
        properties: {
          definition: {
            actions: {
              firstAction: {
                runAfter: {}
              }
              scope: {
                type: 'scope'
                actions: {
                  nestedAction: {
                    runAfter: {}
                  }
                  nestedScope: {
                    type: 'Scope'
                    actions: {
                      deeplyNestedAction: {
                        runAfter: {}
                      }
                    }
                  }
                }
              }
              condition: {
                type: 'If'
                actions: {
                  trueAction: {
                    runAfter: {}
                  }
                }
                else: {
                  actions: {
                    falseAction: {
                      runAfter: {}
                    }
                  }
                }
              }
              forEach: {
                type: 'foreach'
                actions: {
                  loopAction: {
                    runAfter: {}
                  }
                }
              }
              until: {
                type: 'until'
                actions: {
                  untilAction: {
                    runAfter: {}
                  }
                }
              }
              switch: {
                type: 'switch'
                cases: {
                  case: {
                    actions: {
                      caseAction: {
                        runAfter: {}
                      }
                    }
                  }
                  default: {
                    actions: {
                      caseNamedDefaultAction: {
                        runAfter: {}
                      }
                    }
                  }
                }
                default: {
                  actions: {
                    defaultAction: {
                      runAfter: {}
                    }
                  }
                }
              }
            }
          }
        }
      }
      resource secondWorkflow 'microsoft.logic/WORKFLOWS@2024-10-01' = {
        name: 'secondWorkflow'
        location: 'westeurope'
        properties: {
          definition: {
            actions: {
              anotherAction: {
                runAfter: {}
              }
            }
          }
        }
      }
      """, CHECK);
  }

  @Test
  void shouldReportOtherEmptyLogicAppValuesBicep() {
    BicepVerifier.verifyContent("""
      resource workflow 'Microsoft.Logic/workflows@2019-05-01' = {
        name: 'workflow'
        location: 'westeurope'
        properties: {
          definition: {
            actions: {
              action: {
                inputs: {}
                metadata: {
                  runAfter: {}
                }
                runAfter: []
              }
              actionWithInputData: {
                inputs: {
                  actions: {
                    payload: {
                      runAfter: {}
                    }
                  }
                }
              }
            }
            runAfter: {}
          }
        }
      }
      resource storage 'Microsoft.Storage/storageAccounts@2023-01-01' = {
        name: 'storage'
        location: 'westeurope'
        properties: {
          definition: {
            actions: {
              action: {
                runAfter: {}
              }
            }
          }
        }
      }
      """, CHECK,
      issue(8, 10, 8, 20, "Remove this empty object or complete with real code."),
      issue(10, 12, 10, 24, "Remove this empty object or complete with real code."),
      issue(12, 10, 12, 22, "Remove this empty array or complete with real code."),
      issue(18, 16, 18, 28, "Remove this empty object or complete with real code."),
      issue(24, 6, 24, 18, "Remove this empty object or complete with real code."),
      issue(35, 10, 35, 22, "Remove this empty object or complete with real code."));
  }

  @Test
  void shouldIgnoreConfiguredProperties() {
    var emptyOrNullValueCheck = new EmptyOrNullValueCheck();
    emptyOrNullValueCheck.ignoredProperties = "var1,var2,prop1,prop2,out1,out2";
    ArmVerifier.verifyNoIssue("EmptyOrNullValueCheckTest/emptyOrNullValue-ignored.json", emptyOrNullValueCheck);
  }

  @Test
  void shouldAllowKeyVaultEmptyAccessPoliciesJson() {
    // SONARIAC-2046: KeyVault accessPolicies is commonly empty when using RBAC-based access control
    // SONARIAC-2688: exemption is version-agnostic, applies to all API versions of Microsoft.KeyVault/vaults
    ArmVerifier.verify("EmptyOrNullValueCheckTest/emptyOrNullValue-keyVaultException.json", CHECK,
      // KeyVault with other empty properties should still raise
      issue(64, 8, 64, 25, "Remove this empty object or complete with real code."),
      // Other resource types should still raise for empty arrays
      issue(78, 10, 78, 23, "Remove this empty array or complete with real code."),
      // KeyVault with empty API version - the empty string itself raises
      issue(99, 6, 99, 22, "Remove this empty string or complete with real code."));
  }

  @Test
  void shouldAllowKeyVaultEmptyAccessPoliciesBicep() {
    // SONARIAC-2046: KeyVault accessPolicies is commonly empty when using RBAC-based access control
    // SONARIAC-2688: exemption is version-agnostic, applies to all API versions of Microsoft.KeyVault/vaults
    BicepVerifier.verify("EmptyOrNullValueCheckTest/emptyOrNullValue-keyVaultException.bicep", CHECK);
  }

  @Test
  void shouldAllowKeyVaultEmptyAccessPoliciesAllVersionsBicep() {
    // SONARIAC-2688: exemption applies to all API versions (no version, 2022-07-01, 2021-06-01-preview)
    BicepVerifier.verifyContentNoIssue("""
      resource kv1 'Microsoft.KeyVault/vaults' = {
        name: 'kv1'
        location: 'eastus'
        properties: {
          tenantId: subscription().tenantId
          sku: {
            family: 'A'
            name: 'standard'
          }
          enableRbacAuthorization: true
          accessPolicies: []
        }
      }
      resource kv2 'Microsoft.KeyVault/vaults@2022-07-01' = {
        name: 'kv2'
        location: 'eastus'
        properties: {
          tenantId: subscription().tenantId
          sku: {
            family: 'A'
            name: 'standard'
          }
          enableRbacAuthorization: true
          accessPolicies: []
        }
      }
      resource kv3 'Microsoft.KeyVault/vaults@2021-06-01-preview' = {
        name: 'kv3'
        location: 'eastus'
        properties: {
          tenantId: subscription().tenantId
          sku: {
            family: 'A'
            name: 'standard'
          }
          enableRbacAuthorization: true
          accessPolicies: []
        }
      }
      """, CHECK);
  }

  @Test
  void shouldAllowNsgEmptySecurityRulesJson() {
    // Network Security Groups (NSG) securityRules is intentionally empty when no rules are defined
    ArmVerifier.verify("EmptyOrNullValueCheckTest/emptyOrNullValue-nsgException.json", CHECK,
      issue(36, 10, 36, 23, "Remove this empty array or complete with real code."));
  }

  @Test
  void shouldAllowNsgEmptySecurityRulesBicep() {
    // Network Security Groups (NSG) securityRules is intentionally empty when no rules are defined
    BicepVerifier.verify("EmptyOrNullValueCheckTest/emptyOrNullValue-nsgException.bicep", CHECK);
  }

  @Test
  void shouldAllowKeyVaultWithIgnoredPropertiesJson() {
    // SONARIAC-2046: Test combining resource type exception with user-configured ignored properties
    // This covers the branch where isPropertyException=false but ignoredPropertiesSet.contains=true
    var emptyOrNullValueCheck = new EmptyOrNullValueCheck();
    emptyOrNullValueCheck.ignoredProperties = "networkAcls";
    ArmVerifier.verify("EmptyOrNullValueCheckTest/emptyOrNullValue-keyVaultException.json", emptyOrNullValueCheck,
      // KeyVault with empty API version - the empty string itself raises
      issue(99, 6, 99, 22, "Remove this empty string or complete with real code."));
    // networkAcls is ignored via ignoredProperties config, so no issue for line 64
    // ipRules (line 78) is also not checked because it's inside networkAcls which is skipped
  }
}
