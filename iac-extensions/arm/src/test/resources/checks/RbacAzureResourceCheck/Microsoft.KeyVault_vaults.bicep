resource compliant1 'Microsoft.KeyVault/vaults@2022-07-01' = {
  name: 'Compliant'
  properties: {
    enableRbacAuthorization: true
  }
}

resource noncompliant1 'Microsoft.KeyVault/vaults@2022-07-01' = {
  name: 'One issue: property is explicitly set to false'
  properties: {
    enableRbacAuthorization: false // Noncompliant{{Make sure that disabling role-based access control is safe here.}}
  }
}

// Noncompliant@+1{{Omitting 'enableRbacAuthorization' disables role-based access control for this resource. Make sure it is safe here.}}
resource noncompliant2 'Microsoft.KeyVault/vaults@2022-07-01' = {
  name: 'One issue: property is missing'
  properties: {}
}
