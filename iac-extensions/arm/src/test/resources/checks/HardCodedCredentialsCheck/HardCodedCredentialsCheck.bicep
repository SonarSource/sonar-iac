resource sqlServer 'Microsoft.Sql/servers@2023-05-01-preview' = {
  name: 'noncompliant'
  properties: {
    administratorLogin: 'admin' // Noncompliant {{Revoke and change this secret, as it might be compromised.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^
    administratorLoginPassword: 'password' // Noncompliant
    password: 'password' // Noncompliant
    secret: 'password' // Noncompliant
    adminPassword: 'password' // Noncompliant
    adminUsername: 'admin' // Noncompliant
    publishingPassword: 'password' // Noncompliant
    publishingUserName: 'admin' // Noncompliant
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
