resource sqlServer 'Microsoft.Sql/servers@2023-05-01-preview' = {
  name: 'noncompliant'
  properties: {
    administratorLogin: 'admin'
    administratorLoginPassword: 'password' // Noncompliant {{Revoke and change this secret, as it might be compromised.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    password: 'password' // Noncompliant
    secret: 'password' // Noncompliant
    adminPassword: 'password' // Noncompliant
    adminUsername: 'admin'
    publishingPassword: 'password' // Noncompliant
    publishingUserName: 'admin'
    randomProperty: 'password'
  }
}

resource sqlServer 'Microsoft.Sql/servers@2023-05-01-preview' = {
  name: 'compliant'
  properties: {
    administratorLogin: adminLogin
    administratorLoginPassword: adminPassword
    password: password
    secret: secret
    adminPassword: adminPassword
    adminUsername: ''
    publishingPassword: '  '
  }
}

resource compliantLogicApp 'Microsoft.Logic/workflows@2019-05-01' = {
  name: 'compliant-logic-app-expressions'
  properties: {
    administratorLoginPassword: '@{body(GetSecretAPIToken)}'
    password: '@variables(myPassword)'
    secret: '@{concat(variables(prefix), parameters(suffix))}'
    adminPassword: '@body(GetAdminCredentials)'
    publishingPassword: '@{parameters(publishingPassword)}'
  }
}

resource nonCompliant2 'Microsoft.Network/networkInterfaces@2020-06-01' = {
  location: resourceGroup().location
  properties: condition ? {
    // Noncompliant@+1
    administratorLoginPassword: 'password'
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  } : {
    // Noncompliant@+1
    password: 'password'
//  ^^^^^^^^^^^^^^^^^^^^
  }
}

// Plain passwords that happen to start with @ are not Logic App expressions and must still be flagged.
resource noncompliantAtPrefix 'Microsoft.Sql/servers@2023-05-01-preview' = {
  name: 'noncompliant-at-prefix'
  properties: {
    administratorLoginPassword: '@password123' // Noncompliant
    password: '@MySecret' // Noncompliant
    secret: '@admin' // Noncompliant
  }
}
