// Hard-coded Authorization header
resource workflow1 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'hardcoded-secret'
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
            headers: {
              Authorization: 'Bearer abc123-secret-token' // Noncompliant {{Do not hard-code secrets in workflow definitions. Use parameters referencing Azure Key Vault instead.}}
//            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Hard-coded authentication password
resource workflow2 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'hardcoded-password'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Call_API: {
          type: 'Http'
          inputs: {
            method: 'GET'
            uri: 'https://api.example.com'
            authentication: {
              type: 'Basic'
              username: 'admin'
              password: 'P@ssw0rd123!' // Noncompliant
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - secret referenced via workflow parameter
resource workflow3 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'parameterized-secret'
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
            headers: {
              Authorization: '@{concat(\'Bearer \', parameters(\'ApiToken\'))}'
            }
          }
        }
      }
      triggers: {}
      parameters: {
        ApiToken: {
          type: 'securestring'
        }
      }
    }
  }
}

// Compliant - no sensitive properties present
resource workflow4 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'no-sensitive'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Send_Email: {
          type: 'ApiConnection'
          inputs: {
            method: 'post'
            body: 'Hello World'
            path: '/v2/Mail'
          }
        }
      }
      triggers: {}
    }
  }
}

// Hard-coded x-api-key header
resource workflow5 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'hardcoded-api-key'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Call_API: {
          type: 'Http'
          inputs: {
            method: 'GET'
            uri: 'https://api.example.com'
            headers: {
              'x-api-key': 'sk-live-abc123def456' // Noncompliant
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Hard-coded ocp-apim-subscription-key header
resource workflow6 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'hardcoded-apim-subscription-key'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Call_API: {
          type: 'Http'
          inputs: {
            method: 'GET'
            uri: 'https://api.example.com'
            headers: {
              'Ocp-Apim-Subscription-Key': 'apim-key-deadbeef' // Noncompliant
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Hard-coded Raw authentication value
resource workflow7 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'hardcoded-raw-token'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Call_API: {
          type: 'Http'
          inputs: {
            method: 'GET'
            uri: 'https://api.example.com'
            authentication: {
              type: 'Raw'
              value: 'Bearer eyJhbGciOi...' // Noncompliant
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Hard-coded ActiveDirectoryOAuth client secret
resource workflow8 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'hardcoded-oauth-secret'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Call_API: {
          type: 'Http'
          inputs: {
            method: 'POST'
            uri: 'https://api.example.com'
            authentication: {
              type: 'ActiveDirectoryOAuth'
              tenant: 'tenant-guid'
              audience: 'https://api.example.com'
              clientId: 'client-guid'
              secret: 'super-secret-value' // Noncompliant
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Hard-coded ClientCertificate pfx blob
resource workflow9 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'hardcoded-pfx'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Call_API: {
          type: 'Http'
          inputs: {
            method: 'GET'
            uri: 'https://api.example.com'
            authentication: {
              type: 'ClientCertificate'
              pfx: 'MIIKVgIBAzCCCh...' // Noncompliant
              password: 'cert-pass'    // Noncompliant
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Hard-coded Authorization header inside a nested Scope action
resource workflow10 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'nested-scope-auth-header'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Outer_Scope: {
          type: 'Scope'
          actions: {
            Call_API: {
              type: 'Http'
              inputs: {
                method: 'POST'
                uri: 'https://api.example.com'
                headers: {
                  Authorization: 'Bearer nested-token' // Noncompliant
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

// Microsoft.Web/connections - hard-coded password in parameterValues
resource sqlConnection 'Microsoft.Web/connections@2016-06-01' = {
  name: 'sql-connection-hardcoded-password'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'sql')
    }
    parameterValues: {
      server: 'myserver.database.windows.net'
      database: 'mydb'
      username: 'admin'
      password: 'P@ssw0rd123!' // Noncompliant
    }
  }
}

// Microsoft.Web/connections - parameterized password (compliant)
resource sqlConnectionCompliant 'Microsoft.Web/connections@2016-06-01' = {
  name: 'sql-connection-parameterized-password'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'sql')
    }
    parameterValues: {
      server: 'myserver.database.windows.net'
      database: 'mydb'
      username: 'admin'
      password: '[parameters(\'sqlPassword\')]'
    }
  }
}

// Hard-coded Authorization header inside a Switch case
resource workflow11 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'switch-case-hardcoded-secret'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Route: {
          type: 'Switch'
          expression: '@triggerBody()?[\'type\']'
          cases: {
            CaseA: {
              case: 'A'
              actions: {
                Call_API: {
                  type: 'Http'
                  inputs: {
                    method: 'POST'
                    uri: 'https://api.example.com/a'
                    headers: {
                      Authorization: 'Bearer case-token' // Noncompliant
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
      triggers: {}
    }
  }
}

// Hard-coded api-key header inside a Switch default branch
resource workflow12 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'switch-default-hardcoded-secret'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Route: {
          type: 'Switch'
          expression: '@triggerBody()?[\'type\']'
          cases: {}
          default: {
            actions: {
              Call_API: {
                type: 'Http'
                inputs: {
                  method: 'GET'
                  uri: 'https://api.example.com/default'
                  headers: {
                    'x-api-key': 'sk-default-deadbeef' // Noncompliant
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

// Hard-coded password inside a Parallel branch
resource workflow13 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'parallel-branch-hardcoded-password'
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
                Call_API: {
                  type: 'Http'
                  inputs: {
                    method: 'GET'
                    uri: 'https://api.example.com/branch1'
                    authentication: {
                      type: 'Basic'
                      username: 'admin'
                      password: 'P@rallelPwd!' // Noncompliant
                    }
                  }
                }
              }
            }
            Branch2: {
              actions: {
                Other_Call: {
                  type: 'Http'
                  inputs: {
                    method: 'GET'
                    uri: 'https://api.example.com/branch2'
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
