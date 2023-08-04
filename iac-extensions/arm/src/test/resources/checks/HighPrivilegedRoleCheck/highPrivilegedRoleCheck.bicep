param roleParameter string
param newOrExistingRole string

var roleDefinitionId = 'string'

resource noncompliant1 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: 'Sensitive: High-privilege ${role} role (Format 1)'
  properties: {
    description: 'Assign the reader role'
    principalId: 'string'
    principalType: 'ServicePrincipal'
    // Noncompliant@+1{{Make sure that assigning the ${roleName} role is safe here.}}
    roleDefinitionId: resourceId('Microsoft.Authorization/roleDefinitions', '${role}')
  }
}

resource noncompliant2 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: 'Sensitive: High-privilege ${role} role (Format 2)'
  properties: {
    description: 'Assign the reader role'
    principalId: 'string'
    principalType: 'ServicePrincipal'
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '${role}') // Noncompliant
  }
}

resource noncompliant3 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: 'Sensitive: High-privilege ${role} role (Format 3)'
  properties: {
    description: 'Assign the reader role'
    principalId: 'string'
    principalType: 'ServicePrincipal'
    roleDefinitionId: '/subscriptions/{subscriptionId}/providers/Microsoft.Authorization/roleDefinitions/${role}' // Noncompliant
  }
}

resource noncompliant4 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: 'Sensitive: High-privilege ${role} role (Format 4)'
  properties: {
    description: 'Assign the reader role'
    principalId: 'string'
    principalType: 'ServicePrincipal'
    roleDefinitionId: '/providers/Microsoft.Authorization/roleDefinitions/${role}' // Noncompliant
  }
}

resource noncompliant5 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: 'Sensitive: High-privilege ${role} role (Format 5)'
  properties: {
    description: 'Assign the reader role'
    principalId: 'string'
    principalType: 'ServicePrincipal'
    roleDefinitionId: '/subscriptions/${subscription().subscriptionId}/providers/Microsoft.Authorization/roleDefinitions/${role}' // Noncompliant
  }
}

/* TODO SONARIAC-1064 ARM Bicep: parsing error on TernaryExpression with callFunction
resource noncompliant6 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: 'Sensitive: High-privilege ${role} role (Format 6)'
  properties: {
    description: 'Assign the reader role'
    principalId: 'string'
    principalType: 'ServicePrincipal'
    roleDefinitionId: ((newOrExistingRole == 'new') ? resourceId('Microsoft.Authorization/roleDefinitions/', roleDefinitionId) : resourceId('Microsoft.Authorization/roleDefinitions/', '${role}')) // Noncompliant
  }
}
*/

resource compliant1 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: 'Compliant: roleDefinitionId is not string literal'
  properties: {
    description: 'Assign the reader role'
    principalId: 'string'
    principalType: 'ServicePrincipal'
    roleDefinitionId: {}
  }
}
