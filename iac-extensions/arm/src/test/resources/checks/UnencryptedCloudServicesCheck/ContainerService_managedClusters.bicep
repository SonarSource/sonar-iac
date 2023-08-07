// Noncompliant@+1 {{Omitting "diskEncryptionSetID" enables clear-text storage. Make sure it is safe here.}}
resource nonCompliant1 'Microsoft.ContainerService/managedClusters@2023-03-02-preview' = {
  name: 'Noncompliant: enableEncryptionAtHost is set to false'
  properties: {
    agentPoolProfiles: [
      {
        // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
        enableEncryptionAtHost: false
      }
    ]
  }
}

// Noncompliant@+1 {{Omitting "diskEncryptionSetID" enables clear-text storage. Make sure it is safe here.}}
resource nonCompliant2 'Microsoft.ContainerService/managedClusters@2023-03-02-preview' = {
  name: 'Noncompliant: enableEncryptionAtHost is missing'
  properties: {
    agentPoolProfiles: [
      {} // Noncompliant {{Omitting "enableEncryptionAtHost" enables clear-text storage. Make sure it is safe here.}}
    ]
  }
}

// Noncompliant@+1 {{Omitting "diskEncryptionSetID" enables clear-text storage. Make sure it is safe here.}}
resource compliant 'Microsoft.ContainerService/managedClusters@2023-03-02-preview' = {
  name: 'Compliant: enableEncryptionAtHost is set to true'
  properties: {
    agentPoolProfiles: [
      {
        enableEncryptionAtHost: true
      }
    ]
  }
}
