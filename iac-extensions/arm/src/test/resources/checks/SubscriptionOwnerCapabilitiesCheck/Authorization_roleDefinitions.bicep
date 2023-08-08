// Noncompliant@+1{{Narrow the number of actions or the assignable scope of this custom role.}}
resource noncompliant1 'Microsoft.Authorization/roleDefinitions@2022-04-01' = {
//                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'Issue: any action is allowed with sensitive assignable scope and subscription.id()'
  properties: {
    permissions: [
      {
        actions: [
          '*'
//        ^^^< {{Allows all actions}}
        ]
        notActions: []
      }
    ]
    assignableScopes: [
      subscription().id
//    ^^^^^^^^^^^^^^^^^< {{High scope level}}
    ]
  }
}

// Noncompliant@+1
resource noncompliant2 'Microsoft.Authorization/roleDefinitions@2022-04-01' = {
  name: 'Issue: any action is allowed with sensitive assignable scope and managementGroup.id()'
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
      managementGroup().id
    ]
  }
}

// Noncompliant@+1
resource noncompliant3 'Microsoft.Authorization/roleDefinitions@2022-04-01' = {
//                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'Issue: any action is allowed with sensitive assignable scope and sensitive subscription path'
  properties: {
    permissions: [
      {
        actions: [
          '*'
//        ^^^< {{Allows all actions}}
        ]
        notActions: []
      }
    ]
    assignableScopes: [
      '/subscriptions/b24988ac-6180-42a0-ab88-20f7382dd24c'
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{High scope level}}
    ]
  }
}

// Noncompliant@+1
resource noncompliant4 'Microsoft.Authorization/roleDefinitions@2022-04-01' = {
  name: 'Issue: any action is allowed with sensitive assignable scope and sensitive managementGroups path'
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
      '/providers/Microsoft.Management/managementGroups/b24988ac-6180-42a0-ab88-20f7382dd24c'
    ]
  }
}

resource compliant1 'Microsoft.Authorization/roleDefinitions@2022-04-01' = {
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

resource compliant2 'Microsoft.Authorization/roleDefinitions@2022-04-01' = {
  name: 'Compliant - scope is limited to a resource group'
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
      '/subscriptions/b24988ac-6180-42a0-ab88-20f7382dd24c/resourceGroups/b24988ac-6180-42a0-ab88-20f7382dd24c'
    ]
  }
}

resource compliant3 'Microsoft.Authorization/roleDefinitions@2022-04-01' = {
  name: 'Compliant - the member expression separator is not a dot and so not identified as a sensitive scopes'
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
      subscription()::id
    ]
  }
}

resource compliant4 'Microsoft.Authorization/roleDefinitions@2022-04-01' = {
  name: 'Compliant - the accessed property to subscription() is not id, so not considered as a sensitive call'
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
      subscription().value
    ]
  }
}
