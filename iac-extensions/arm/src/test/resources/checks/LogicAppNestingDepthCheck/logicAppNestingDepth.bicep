// Nesting depth 4 exceeds default max of 3
resource workflow1 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'deeply-nested-workflow'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Check_condition: {
          type: 'If'
          expression: '@equals(triggerBody()?[\'status\'], \'active\')'
          actions: {
            Loop_items: {
              type: 'Foreach'
              foreach: '@triggerBody()?[\'items\']'
              actions: {
                Check_item: {
                  type: 'If'
                  expression: '@greater(items(\'Loop_items\')?[\'value\'], 0)'
                  actions: {
                    Retry_until_done: { // Noncompliant {{Refactor this Logic App workflow to reduce control action nesting depth from 4 to at most 3.}}
                      type: 'Until'
                      expression: '@equals(body(\'Process\')?[\'done\'], true)'
                      limit: { count: 10 }
                      actions: {
                        Process: {
                          type: 'Http'
                          inputs: {
                            method: 'POST'
                            uri: 'https://api.example.com/process'
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Nesting exceeds max inside switch case branch: If(1)->Switch(2)->Foreach(3)->Scope(4)
resource workflow4 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'noncompliant-inside-switch-case'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Outer_If: {
          type: 'If'
          expression: '@true'
          actions: {
            Route: {
              type: 'Switch'
              expression: '@triggerBody()?[\'type\']'
              cases: {
                CaseA: {
                  case: 'A'
                  actions: {
                    Inner_Loop: {
                      type: 'Foreach'
                      foreach: '@triggerBody()?[\'items\']'
                      actions: {
                        Deep_Scope: { // Noncompliant {{Refactor this Logic App workflow to reduce control action nesting depth from 4 to at most 3.}}
                          type: 'Scope'
                          actions: {}
                        }
                      }
                    }
                  }
                }
              }
              default: {
                actions: {}
              }
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Nesting exceeds max inside switch default branch: Scope(1)->Switch(2)->Foreach(3)->Until(4)
resource workflow5 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'noncompliant-inside-switch-default'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Scope1: {
          type: 'Scope'
          actions: {
            Route: {
              type: 'Switch'
              expression: '@triggerBody()?[\'type\']'
              cases: {}
              default: {
                actions: {
                  Default_Loop: {
                    type: 'Foreach'
                    foreach: '@triggerBody()?[\'items\']'
                    actions: {
                      Too_Deep: { // Noncompliant
                        type: 'Until'
                        expression: '@true'
                        limit: { count: 5 }
                        actions: {}
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Nesting exceeds max inside if-else branch: If(1)->If(2)->Foreach(3)->Scope(4)
resource workflow6 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'noncompliant-inside-if-else'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Check1: {
          type: 'If'
          expression: '@true'
          actions: {}
          else: {
            actions: {
              Check2: {
                type: 'If'
                expression: '@true'
                actions: {
                  Loop: {
                    type: 'Foreach'
                    foreach: '@triggerBody()?[\'items\']'
                    actions: {
                      Nested_Scope: { // Noncompliant
                        type: 'Scope'
                        actions: {}
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - nesting depth exactly 3
resource workflow2 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'acceptable-nesting'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Check_condition: {
          type: 'If'
          expression: '@equals(triggerBody()?[\'status\'], \'active\')'
          actions: {
            Loop_items: {
              type: 'Foreach'
              foreach: '@triggerBody()?[\'items\']'
              actions: {
                Inner_Scope: {
                  type: 'Scope'
                  actions: {
                    Send_email: {
                      type: 'ApiConnection'
                      inputs: {
                        method: 'post'
                        path: '/v2/Mail'
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - flat workflow
resource workflow3 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'flat-workflow'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Get_Data: {
          type: 'Http'
          inputs: { method: 'GET', uri: 'https://api.example.com/data' }
          runAfter: {}
        }
        Send_Notification: {
          type: 'ApiConnection'
          inputs: { method: 'post', body: '@body(\'Get_Data\')', path: '/v2/Mail' }
          runAfter: { Get_Data: ['Succeeded'] }
        }
      }
      triggers: {}
    }
  }
}

// Switch action itself is the violating node: Scope(1)->Foreach(2)->If(3)->Switch(4)
resource workflow7 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'noncompliant-switch-depth-4'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Outer_Scope: {
          type: 'Scope'
          actions: {
            Loop: {
              type: 'Foreach'
              foreach: '@triggerBody()?[\'records\']'
              actions: {
                Check_Type: {
                  type: 'If'
                  expression: '@not(empty(items(\'Loop\')?[\'type\']))'
                  actions: {
                    Route_By_Type: { // Noncompliant
                      type: 'Switch'
                      expression: '@items(\'Loop\')?[\'type\']'
                      cases: {
                        TypeA: {
                          case: 'A'
                          actions: {
                            Handle_A: { type: 'Http', inputs: { method: 'POST', uri: 'https://api.example.com/a' } }
                          }
                        }
                      }
                      default: {
                        actions: {}
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - definition missing
resource workflow8 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'no-definition'
  location: resourceGroup().location
  properties: {}
}

// Compliant - empty definition
resource workflow9 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'empty-definition'
  location: resourceGroup().location
  properties: {
    definition: {}
  }
}

// Compliant - definition without actions
resource workflow10 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'no-actions'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      triggers: {}
    }
  }
}

// Compliant - empty actions block
resource workflow11 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'empty-actions'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {}
      triggers: {}
    }
  }
}

// Compliant - action type is a parameter expression, not a literal control type
resource workflow12 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'parameterized-type'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Action1: {
          type: '[parameters(\'actType\')]'
          actions: {
            Action2: {
              type: '[parameters(\'actType\')]'
              actions: {
                Action3: {
                  type: '[parameters(\'actType\')]'
                  actions: {
                    Action4: {
                      type: '[parameters(\'actType\')]'
                      actions: {}
                    }
                  }
                }
              }
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Showcase: depth-4 violation with main issue + secondary location highlighting on enclosing control actions
resource workflow13 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'noncompliant-with-secondaries'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Top_If: {
//      ^^^^^^> {{Enclosing control action.}}
          type: 'If'
          expression: '@true'
          actions: {
            Loop: {
//          ^^^^> {{Enclosing control action.}}
              type: 'Foreach'
              foreach: '@triggerBody()'
              actions: {
                Mid_If: {
//              ^^^^^^> {{Enclosing control action.}}
                  type: 'If'
                  expression: '@true'
                  actions: {
                    Deep_Until: { type: 'Until' actions: {} } // Noncompliant {{Refactor this Logic App workflow to reduce control action nesting depth from 4 to at most 3.}}
//                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                  }
                }
              }
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Two sibling violations under the same If: one in actions (then-branch), one in else.actions
resource workflow14 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'sibling-violations-in-if'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Outer_Scope: {
          type: 'Scope'
          actions: {
            Loop: {
              type: 'Foreach'
              foreach: '@triggerBody()'
              actions: {
                Branch: {
                  type: 'If'
                  expression: '@true'
                  actions: {
                    Then_Scope: { // Noncompliant
                      type: 'Scope'
                      actions: {}
                    }
                  }
                  else: {
                    actions: {
                      Else_Scope: { // Noncompliant
                        type: 'Scope'
                        actions: {}
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      triggers: {}
    }
  }
}
