resource compliant 'Microsoft.Authorization/roleDefinitions@2022-04-01' = {
  name: 'Compliant - specific actions are specified'
  properties: {
    permissions: [
      {
        actions: [
          'Microsoft.Compute/*'
        ]
        notActions: []
      }
    ]
    assignableScopes: [
      subscription().id
    ]
  }
}

resource noncompliant 'Microsoft.Authorization/roleDefinitions@2022-04-01' = {
  name: 'Issue: any action is allowed with sensitive assignable scope and subscription.id()'
  properties: {
    permissions: [
      {
        actions: [
          '*'
        ]
        notActions: []
      }
    ]
    assignableScopes: [
      subscription().id
    ]
  }
}
