resource nonCompliant1 'Microsoft.AzureArcData/sqlServerInstances/databases@2023-03-15-preview' = {
  name: 'Noncompliant: isEncrypted is set to false'
  properties: {
    databaseOptions: {
      // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
      isEncrypted: false
//    ^^^^^^^^^^^^^^^^^^
    }
  }
}

resource nonCompliant2 'Microsoft.AzureArcData/sqlServerInstances/databases@2023-03-15-preview' = {
  name: 'Noncompliant: isEncrypted is not set'
  properties: {
    databaseOptions: {}  // Noncompliant {{Omitting "isEncrypted" enables clear-text storage. Make sure it is safe here.}}
//                   ^^
  }
}

resource compliant 'Microsoft.AzureArcData/sqlServerInstances/databases@2023-03-15-preview' = {
  name: 'Compliant: isEncrypted is set to true'
  properties: {
    databaseOptions: {
      isEncrypted: true
    }
  }
}
