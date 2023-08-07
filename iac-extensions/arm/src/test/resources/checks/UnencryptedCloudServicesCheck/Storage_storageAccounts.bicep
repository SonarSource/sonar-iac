resource compliant1 'Microsoft.Storage/storageAccounts@2023-03-02-preview' = {
  name: 'Compliant'
  properties: {
    encryption: {
      requireInfrastructureEncryption: true
    }
  }
}

resource compliant2 'Microsoft.Storage/storageAccounts/encryptionScopes@2023-03-02-preview' = {
  name: 'Compliant'
  properties: {
    requireInfrastructureEncryption: true
  }
}

resource nonCompliant1 'Microsoft.Storage/storageAccounts@2023-03-02-preview' = {
  name: 'Non-compliant: encryption is disabled'
  properties: {
    encryption: {
      // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
      requireInfrastructureEncryption: false
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource nonCompliant2 'Microsoft.Storage/storageAccounts/encryptionScopes@2023-03-02-preview' = {
  name: 'Non-compliant: encryption is disabled'
  properties: {
    // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
    requireInfrastructureEncryption: false
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

// Noncompliant@+1 {{Omitting "encryption" enables clear-text storage. Make sure it is safe here.}}
resource nonCompliant3 'Microsoft.Storage/storageAccounts@2023-03-02-preview' = {
//                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'Non-compliant: encryption-related property is omitted'
  properties: {}
}

resource nonCompliant4 'Microsoft.Storage/storageAccounts@2023-03-02-preview' = {
  name: 'Non-compliant: encryption object is present, requireInfrastructureEncryption is omitted'
  properties: {
    encryption: {} // Noncompliant {{Omitting "requireInfrastructureEncryption" enables clear-text storage. Make sure it is safe here.}}
//              ^^
  }
}

// Noncompliant@+1 {{Omitting "requireInfrastructureEncryption" enables clear-text storage. Make sure it is safe here.}}
resource nonCompliant7 'Microsoft.Storage/storageAccounts/encryptionScopes@2023-03-02-preview' = {
//                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'Non-compliant: encryption-related property is omitted'
  properties: {}
}
