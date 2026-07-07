targetScope = 'subscription'

// Two assignments of the same built-in role (Reader) to the same principal type (User): telemetry counters aggregate to 2.

resource readerUserFirst 'Microsoft.Authorization/roleAssignments@2022-04-01' = { // Noncompliant
  name: 'Reader, User, first'
  properties: {
    roleDefinitionId: '/providers/Microsoft.Authorization/roleDefinitions/acdd72a7-3385-48ef-bd42-f606fba81ae7'
    principalId: '00000000-0000-0000-0000-000000000000'
    principalType: 'User'
  }
}

resource readerUserSecond 'Microsoft.Authorization/roleAssignments@2022-04-01' = { // Noncompliant
  name: 'Reader, User, second'
  properties: {
    roleDefinitionId: '/providers/Microsoft.Authorization/roleDefinitions/acdd72a7-3385-48ef-bd42-f606fba81ae7'
    principalId: '00000000-0000-0000-0000-000000000000'
    principalType: 'User'
  }
}
