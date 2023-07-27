@secure()
param adminUsername string = newGuid()

// Noncompliant@+1 {{Change this code to not use an outer expression evaluation scope in nested templates.}}
resource noncompliantDeployment 'Microsoft.Resources/deployments@2022-09-01' = {
  name: 'nestedDeployment-noncompliant'
  properties: {
    // expressionEvaluationOptions is missing (defaults to 'Outer')
    mode: 'Incremental'
    template: {
      '$schema': 'https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#'
      contentVersion: '1.0.0.0'
      resources: [
        {
          apiVersion: '2023-03-01'
          type: 'Microsoft.Compute/virtualMachines'
          name: 'vm-example'
          location: 'northeurope'
          properties: {
            osProfile: {
              computerName: 'vm-example'
                adminUsername: adminUsername
            }
          }
        }
      ]
    }
  }
}
