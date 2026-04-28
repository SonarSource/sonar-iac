// With max=2, depth 3 (If->Foreach->Scope) becomes noncompliant
resource workflow1 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'depth-3-noncompliant-when-max-2'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Outer_If: {
          type: 'If'
          expression: '@true'
          actions: {
            Loop: {
              type: 'Foreach'
              foreach: '@triggerBody()'
              actions: {
                Inner_Scope: { // Noncompliant {{Refactor this Logic App workflow to reduce control action nesting depth from 3 to at most 2.}}
                  type: 'Scope'
                  actions: {}
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

// Compliant - depth 2 with max=2
resource workflow2 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'depth-2-compliant'
  location: resourceGroup().location
  properties: {
    definition: {
      '$schema': 'https://schema.management.azure.com/providers/Microsoft.Logic/schemas/2016-06-01/workflowdefinition.json#'
      actions: {
        Outer_If: {
          type: 'If'
          expression: '@true'
          actions: {
            Inner_Scope: {
              type: 'Scope'
              actions: {}
            }
          }
        }
      }
      triggers: {}
    }
  }
}
