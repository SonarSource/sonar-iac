// Basic authentication
resource workflow1 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'basic-auth-workflow'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Call_API: {
          type: 'Http'
          inputs: {
            method: 'GET'
            uri: 'https://api.example.com/data'
            authentication: { // Noncompliant {{Replace Basic or Raw authentication with Managed Identity or OAuth 2.0 for this Logic Apps action.}}
              type: 'Basic'
              username: 'apiuser'
              password: 'P@ssw0rd!'
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Raw authentication
resource workflow2 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'raw-auth-workflow'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Call_API: {
          type: 'Http'
          inputs: {
            method: 'POST'
            uri: 'https://api.example.com/data'
            authentication: { type: 'Raw' value: 'Bearer sk-live-abc123def456' } // Noncompliant
//                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - Managed Identity
resource workflow3 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'managed-identity-workflow'
  location: resourceGroup().location
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Call_API: {
          type: 'Http'
          inputs: {
            method: 'GET'
            uri: 'https://api.example.com/data'
            authentication: {
              type: 'ManagedServiceIdentity'
              audience: 'https://api.example.com'
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - OAuth 2.0 / Entra ID
resource workflow4 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'oauth-workflow'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Call_API: {
          type: 'Http'
          inputs: {
            method: 'POST'
            uri: 'https://api.example.com/data'
            authentication: {
              type: 'ActiveDirectoryOAuth'
              tenant: tenantId
              audience: 'https://api.example.com'
              clientId: clientId
              secret: clientSecret
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - no authentication block
resource workflow5 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'no-auth-workflow'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Get_Public_Data: {
          type: 'Http'
          inputs: {
            method: 'GET'
            uri: 'https://api.example.com/public'
          }
        }
      }
      triggers: {}
    }
  }
}

// Basic authentication inside a Scope action
resource workflow6 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'scope-with-basic-auth'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        My_Scope: {
          type: 'Scope'
          actions: {
            Call_API: {
              type: 'Http'
              inputs: {
                method: 'GET'
                uri: 'https://api.example.com/data'
                authentication: { // Noncompliant
                  type: 'Basic'
                  username: 'apiuser'
                  password: 'P@ssw0rd!'
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

// Basic authentication inside If action's then-branch and Raw inside its else-branch
resource workflow7 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'if-with-bad-auth'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Branch: {
          type: 'If'
          expression: '@equals(1, 1)'
          actions: {
            Then_Call: {
              type: 'Http'
              inputs: {
                uri: 'https://api.example.com/then'
                authentication: { // Noncompliant
                  type: 'Basic'
                  username: 'u'
                  password: 'p'
                }
              }
            }
          }
          else: {
            actions: {
              Else_Call: {
                type: 'Http'
                inputs: {
                  uri: 'https://api.example.com/else'
                  authentication: { // Noncompliant
                    type: 'Raw'
                    value: 'Bearer xyz'
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

// Basic authentication inside a Switch case and Raw inside the default branch
resource workflow8 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'switch-with-bad-auth'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Pick: {
          type: 'Switch'
          expression: '@variables(\'mode\')'
          cases: {
            CaseA: {
              case: 'a'
              actions: {
                Call_A: {
                  type: 'Http'
                  inputs: {
                    uri: 'https://api.example.com/a'
                    authentication: { // Noncompliant
                      type: 'Basic'
                      username: 'u'
                      password: 'p'
                    }
                  }
                }
              }
            }
          }
          default: {
            actions: {
              Default_Call: {
                type: 'Http'
                inputs: {
                  uri: 'https://api.example.com/default'
                  authentication: { // Noncompliant
                    type: 'Raw'
                    value: 'Bearer xyz'
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

// Deeply nested: ForEach > Until > Http with Basic auth
resource workflow9 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'deeply-nested-bad-auth'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Loop_All: {
          type: 'Foreach'
          foreach: '@variables(\'items\')'
          actions: {
            Retry_Until_Done: {
              type: 'Until'
              expression: '@equals(1, 1)'
              limit: { count: 3, timeout: 'PT1H' }
              actions: {
                Call_API: {
                  type: 'Http'
                  inputs: {
                    uri: 'https://api.example.com/data'
                    authentication: { // Noncompliant
                      type: 'Basic'
                      username: 'u'
                      password: 'p'
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

// Trigger using Basic authentication
resource workflow10 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'trigger-basic-auth'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {}
      triggers: {
        Poll_API: {
          type: 'Http'
          recurrence: { frequency: 'Hour', interval: 1 }
          inputs: {
            method: 'GET'
            uri: 'https://api.example.com/data'
            authentication: { // Noncompliant {{Replace Basic or Raw authentication with Managed Identity or OAuth 2.0 for this Logic Apps trigger.}}
              type: 'Basic'
              username: 'u'
              password: 'p'
            }
          }
        }
      }
    }
  }
}

// Compliant - inputs has no authentication block
resource workflow_no_auth_block 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'inputs-without-authentication'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Call_API: {
          type: 'Http'
          inputs: {
            method: 'GET'
            uri: 'https://api.example.com/data'
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - authentication object has no type property
resource workflow_auth_no_type 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'authentication-without-type'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Call_API: {
          type: 'Http'
          inputs: {
            method: 'GET'
            uri: 'https://api.example.com/data'
            authentication: {
              username: 'u'
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - actions is a string, not an object (malformed template — must not crash)
resource workflow_actions_not_object 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'actions-not-an-object'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: 'foo'
      triggers: {}
    }
  }
}

// Basic authentication inside a Parallel branch
resource workflow11 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'parallel-with-bad-auth'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Fan_Out: {
          type: 'Parallel'
          branches: {
            Branch1: {
              actions: {
                Http_1: {
                  type: 'Http'
                  inputs: {
                    uri: 'https://api.example.com/branch1'
                    authentication: { // Noncompliant
                      type: 'Basic'
                      username: 'u'
                      password: 'p'
                    }
                  }
                }
              }
            }
            Branch2: {
              actions: {
                Http_2: {
                  type: 'Http'
                  inputs: {
                    uri: 'https://api.example.com/branch2'
                    authentication: { // Noncompliant
                      type: 'Raw'
                      value: 'Bearer xyz'
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
