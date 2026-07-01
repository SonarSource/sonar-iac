resource sqlServer 'Microsoft.Sql/servers@2023-05-01-preview' = {
  name: 'noncompliant'
  properties: {
    administratorLogin: 'admin'
    administratorLoginPassword: 'Rb7kZpQ2' // Noncompliant {{Revoke and change this secret, as it might be compromised.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    password: 'Rb7kZpQ2' // Noncompliant
    secret: 'Rb7kZpQ2' // Noncompliant
    adminPassword: 'Rb7kZpQ2' // Noncompliant
    adminUsername: 'admin'
    publishingPassword: 'Rb7kZpQ2' // Noncompliant
    publishingUserName: 'admin'
    randomProperty: 'Rb7kZpQ2'
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
    administratorLoginPassword: 'Rb7kZpQ2'
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  } : {
    // Noncompliant@+1
    password: 'Rb7kZpQ2'
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
