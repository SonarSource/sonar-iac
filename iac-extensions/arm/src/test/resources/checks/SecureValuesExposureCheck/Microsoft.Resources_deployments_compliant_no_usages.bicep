@secure()
param adminUsername string = newGuid()

resource compliantDeployment 'Microsoft.Resources/deployments@2022-09-01' = {
  name: 'nestedDeployment-compliant'
  properties: {
    expressionEvaluationOptions: {
      scope: 'Outer'
    }
    mode: 'Incremental'
    template: {
      '$schema': 'https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#'
      contentVersion: '1.0.0.0'
      parameters: {
        adminUsername: {
          type: 'securestring'
          defaultValue: 'bla' // Note: insecure, however it is impossible to use newGuid here
        }
      }
      resources: [
        {
          apiVersion: '2023-03-01'
          type: 'Microsoft.Compute/virtualMachines'
          name: 'vm-example'
          location: 'northeurope'
          properties: {
            osProfile: {
              computerName: 'vm-example'
              adminUsername: 'secret-string'
            }
          }
        }
      ]
    }
  }
}
