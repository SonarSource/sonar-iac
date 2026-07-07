targetScope = 'subscription'

resource builtInServicePrincipal 'Microsoft.Authorization/roleAssignments@2022-04-01' = { // Noncompliant
  name: 'built-in role resolved from GUID, ServicePrincipal'
  properties: {
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', 'ba92f5b4-2d11-453d-a403-e96b0029c9fe')
    principalId: '00000000-0000-0000-0000-000000000000'
    principalType: 'ServicePrincipal'
  }
}

resource customRoleUser 'Microsoft.Authorization/roleAssignments@2022-04-01' = { // Noncompliant
  name: 'custom role GUID, User'
  properties: {
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '11111111-1111-1111-1111-111111111111')
    principalId: '00000000-0000-0000-0000-000000000000'
    principalType: 'User'
  }
}

resource builtInFromPathGroup 'Microsoft.Authorization/roleAssignments@2022-04-01' = { // Noncompliant
  name: 'built-in role resolved from full path, Group'
  properties: {
    roleDefinitionId: '/providers/Microsoft.Authorization/roleDefinitions/acdd72a7-3385-48ef-bd42-f606fba81ae7'
    principalId: '00000000-0000-0000-0000-000000000000'
    principalType: 'Group'
  }
}

resource withCondition 'Microsoft.Authorization/roleAssignments@2022-04-01' = { // Compliant
  name: 'compliant, condition set, excluded from telemetry'
  properties: {
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', 'ba92f5b4-2d11-453d-a403-e96b0029c9fe')
    principalId: '00000000-0000-0000-0000-000000000000'
    principalType: 'ServicePrincipal'
    condition: '(@Resource[Microsoft.Storage/storageAccounts/blobServices/containers:name] StringEquals \'public\')'
  }
}
