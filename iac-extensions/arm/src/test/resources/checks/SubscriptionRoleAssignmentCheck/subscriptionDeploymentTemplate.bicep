param principalId string
param roleDefinitionId string

targetScope = 'subscription' // Sensitive
//            ^^^^^^^^^^^^^^> {{Subscription scope}}

// Noncompliant@+1 {{Make sure assigning this role with a Subscription scope is safe here.}}
resource nonCompliant1 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
//       ^^^^^^^^^^^^^
  name: 'Sensitive: schema is subscription'
  properties: {
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', roleDefinitionId)
    principalId: principalId
  }
}

// Noncompliant@+1 {{Make sure assigning this role with a Subscription scope is safe here.}}
resource nonCompliant2 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: 'Sensitive: schema is subscription, raise even on empty object'
  properties: {
  }
}

resource compliant1 'other/type@2022-04-01' = {
  name: 'Compliant: not a sensitive type'
  properties: {
  }
}

resource compliant2 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: 'Compliant: condition restricts the effective permissions'
  properties: {
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', roleDefinitionId)
    principalId: principalId
    conditionVersion: '2.0'
    condition: '(@Resource[Microsoft.Storage/storageAccounts/blobServices/containers/blobs:path] StringLike \'public/*\')'
  }
}

// Noncompliant@+1 {{Make sure assigning this role with a Subscription scope is safe here.}}
resource nonCompliant3 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: 'Sensitive: an empty condition imposes no restriction'
  properties: {
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', roleDefinitionId)
    principalId: principalId
    condition: ''
  }
}

resource compliant3 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: 'Compliant: condition is a non-literal expression that cannot be resolved statically'
  properties: {
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', roleDefinitionId)
    principalId: principalId
    condition: roleDefinitionId
  }
}
