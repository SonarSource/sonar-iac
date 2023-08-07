param principalId string
param roleDefinitionId string

resource compliant1 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: 'Compliant: scope defaults to resourceGroup'
  properties: {
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', roleDefinitionId)
    principalId: principalId
  }
}
