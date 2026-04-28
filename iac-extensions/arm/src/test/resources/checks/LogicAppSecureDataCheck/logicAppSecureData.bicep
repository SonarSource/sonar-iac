// No runtimeConfiguration
resource workflow1 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'no-secure-data'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        HTTP_Call: { // Noncompliant {{Enable Secure Inputs and Outputs for this Logic Apps action to prevent sensitive data exposure in run history.}}
          type: 'Http'
          inputs: {
            method: 'POST'
            uri: 'https://api.example.com/sensitive'
            body: '@triggerBody()'
          }
        }
      }
      triggers: {}
    }
  }
}

// secureData.properties missing "outputs"
resource workflow2 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'partial-secure-data'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        HTTP_Call: { // Noncompliant
          type: 'Http'
          inputs: {
            method: 'POST'
            uri: 'https://api.example.com/sensitive'
            body: '@triggerBody()'
          }
          runtimeConfiguration: {
            secureData: {
              properties: [
                'inputs'
              ]
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - both inputs and outputs secured
resource workflow3 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'fully-secured'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        HTTP_Call: {
          type: 'Http'
          inputs: {
            method: 'POST'
            uri: 'https://api.example.com/sensitive'
            body: '@triggerBody()'
          }
          runtimeConfiguration: {
            secureData: {
              properties: [
                'inputs'
                'outputs'
              ]
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// secureData.properties contains only "outputs"
resource workflow4 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'only-outputs'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        HTTP_Call: { // Noncompliant
          type: 'Http'
          runtimeConfiguration: {
            secureData: {
              properties: [
                'outputs'
              ]
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - extra value alongside inputs and outputs is still fine
resource workflow5 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'extra-property'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        HTTP_Call: {
          type: 'Http'
          runtimeConfiguration: {
            secureData: {
              properties: [
                'inputs'
                'outputs'
                'something_else'
              ]
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Empty properties array
resource workflow6 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'empty-properties'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        HTTP_Call: { // Noncompliant
          type: 'Http'
          runtimeConfiguration: {
            secureData: {
              properties: []
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// properties is not an array (string instead)
resource workflow7 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'properties-not-array'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        HTTP_Call: { // Noncompliant
          type: 'Http'
          runtimeConfiguration: {
            secureData: {
              properties: 'foo'
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// No "properties" key under secureData
resource workflow8 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'no-properties-key'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        HTTP_Call: { // Noncompliant
          type: 'Http'
          runtimeConfiguration: {
            secureData: {}
          }
        }
      }
      triggers: {}
    }
  }
}

// runtimeConfiguration without secureData
resource workflow9 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'no-secure-data-key'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        HTTP_Call: { // Noncompliant
          type: 'Http'
          runtimeConfiguration: {
            staticResult: {
              name: 'foo'
              staticResultOptions: 'Enabled'
            }
          }
        }
      }
      triggers: {}
    }
  }
}

// Multiple actions: one compliant, one non-compliant
resource workflow10 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'mixed-actions'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Get_Secret: {
          type: 'ApiConnection'
          runtimeConfiguration: {
            secureData: {
              properties: [
                'inputs'
                'outputs'
              ]
            }
          }
        }
        Send_Email: { // Noncompliant
          type: 'ApiConnection'
        }
      }
      triggers: {}
    }
  }
}

// Trigger non-compliant
resource workflow11 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'trigger-not-secured'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {}
      triggers: {
        Recurrence: { // Noncompliant {{Enable Secure Inputs and Outputs for this Logic Apps trigger to prevent sensitive data exposure in run history.}}
          type: 'Recurrence'
          recurrence: {
            frequency: 'Hour'
            interval: 1
          }
        }
      }
    }
  }
}

// Trigger compliant
resource workflow12 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'trigger-secured'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {}
      triggers: {
        Recurrence: {
          type: 'Recurrence'
          recurrence: {
            frequency: 'Hour'
            interval: 1
          }
          runtimeConfiguration: {
            secureData: {
              properties: [
                'inputs'
                'outputs'
              ]
            }
          }
        }
      }
    }
  }
}
