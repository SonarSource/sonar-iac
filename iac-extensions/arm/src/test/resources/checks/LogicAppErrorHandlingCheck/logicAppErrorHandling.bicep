// No Scope actions at all
resource workflow1 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'no-error-handling'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: { // Noncompliant {{Add structured error handling to this Logic App workflow using Try/Catch Scopes.}}
        Get_items: {
          type: 'ApiConnection'
          inputs: {
            method: 'get'
            path: '/items'
          }
          runAfter: {}
        }
      }
      triggers: {}
    }
  }
}

// Scope exists but no catch Scope
resource workflow2 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'scope-without-catch'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: { // Noncompliant
        Try: {
          type: 'Scope'
          actions: {
            HTTP_call: {
              type: 'Http'
              inputs: { uri: 'https://example.com', method: 'GET' }
            }
          }
          runAfter: {}
        }
        Next_step: {
          type: 'Http'
          inputs: { uri: 'https://example.com/next', method: 'GET' }
          runAfter: {
            Try: [
              'Succeeded'
            ]
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - Try Scope with Catch Scope on failure
resource workflow3 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'with-error-handling'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Try: {
          type: 'Scope'
          actions: {
            HTTP_1: {
              type: 'Http'
              inputs: {
                uri: 'https://example.com'
                method: 'GET'
              }
            }
          }
          runAfter: {}
        }
        Catch: {
          type: 'Scope'
          actions: {
            Log_error: {
              type: 'ApiConnection'
              inputs: {
                method: 'post'
                body: 'Error occurred'
                path: '/log'
              }
            }
          }
          runAfter: {
            Try: [
              'Failed'
              'TimedOut'
            ]
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - names do not matter, pattern is structural
resource workflow4 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'custom-names'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Main_Logic: {
          type: 'Scope'
          actions: {}
          runAfter: {}
        }
        Error_Handler: {
          type: 'Scope'
          actions: {}
          runAfter: {
            Main_Logic: [
              'Failed'
            ]
          }
        }
      }
      triggers: {}
    }
  }
}

// Empty actions block
resource workflow5 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'empty-actions'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {} // Noncompliant
      triggers: {}
    }
  }
}

// Compliant - empty definition (no actions key, nothing to analyze)
resource workflow6 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'empty-definition'
  location: resourceGroup().location
  properties: {
    definition: {}
  }
}

// Catch Scope only references "Skipped" - not an error status, so no real catch
resource workflow7 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'skipped-only'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: { // Noncompliant
        Try: {
          type: 'Scope'
          actions: {}
          runAfter: {}
        }
        Catch: {
          type: 'Scope'
          actions: {}
          runAfter: {
            Try: [
              'Skipped'
            ]
          }
        }
      }
      triggers: {}
    }
  }
}

// Compliant - "TimedOut" alone is a valid error status
resource workflow8 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'timed-out-only'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Try: {
          type: 'Scope'
          actions: {}
          runAfter: {}
        }
        Catch: {
          type: 'Scope'
          actions: {}
          runAfter: {
            Try: [
              'TimedOut'
            ]
          }
        }
      }
      triggers: {}
    }
  }
}

// type 'Not-A-Scope' - never recognized as a Scope
resource workflow9 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'not-a-scope-type'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: { // Noncompliant
        Try: {
          type: 'Not-A-Scope'
          actions: {}
          runAfter: {}
        }
        Catch: {
          type: 'Not-A-Scope'
          actions: {}
          runAfter: {
            Try: [
              'Failed'
            ]
          }
        }
      }
      triggers: {}
    }
  }
}
