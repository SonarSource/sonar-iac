param principalId string
param roleDefinitionId string

targetScope = 'subscription' // Sensitive
//            ^^^^^^^^^^^^^^> {{Subscription scope}}

// Noncompliant@+1 {{Make sure assigning this role with a Subscription scope is safe here.}}
resource nonCompliant1 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
//                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
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

resource nonCompliant2 'other/type@2022-04-01' = {
  name: 'Compliant: not a sensitive type'
  properties: {
  }
}
