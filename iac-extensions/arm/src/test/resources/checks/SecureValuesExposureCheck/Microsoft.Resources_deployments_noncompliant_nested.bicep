@secure()
param adminUsername string = newGuid()

// Noncompliant@+1 {{Change this code to not use an outer expression evaluation scope in nested templates.}}
resource noncompliantDeployment 'Microsoft.Resources/deployments@2022-09-01' = {
//                              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 1
  name: 'Noncompliant: expressionEvaluationOptions is missing (defaults to \'Outer\')'
  properties: {
    // expressionEvaluationOptions is missing (defaults to 'Outer')
    mode: 'Incremental'
    template: {
      '$schema': 'https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#'
      contentVersion: '1.0.0.0'
      resources: [
        {
          type: 'Microsoft.Resources/deployments'
          apiVersion: '2022-09-01'
          name: 'Noncompliant: expressionEvaluationOptions from the root template is used'
          properties: {
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
//                                   ^^^^^^^^^^^^^< {{This secure parameter is leaked through the deployment history.}}
                    }
                  }
                }
              ]
            }
          }
        }
      ]
    }
  }
}
