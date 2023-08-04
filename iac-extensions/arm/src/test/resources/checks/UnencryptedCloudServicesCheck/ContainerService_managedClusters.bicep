// Noncompliant@+1 {{Omitting "diskEncryptionSetID" enables clear-text storage. Make sure it is safe here.}}
resource noncompliantEnableEncryptionAtHostIsSetToFalse 'Microsoft.ContainerService/managedClusters@2023-03-02-preview' = {
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
resource noncompliantEnableEncryptionAtHostIsMissing 'Microsoft.ContainerService/managedClusters@2023-03-02-preview' = {
  name: 'Noncompliant: enableEncryptionAtHost is missing'
  properties: {
    agentPoolProfiles: [
      {} // Noncompliant {{Omitting "enableEncryptionAtHost" enables clear-text storage. Make sure it is safe here.}}
    ]
  }
}

// Noncompliant@+1 {{Omitting "diskEncryptionSetID" enables clear-text storage. Make sure it is safe here.}}
resource compliantEnableEncryptionAtHostIsSetToTrue 'Microsoft.ContainerService/managedClusters@2023-03-02-preview' = {
  name: 'Compliant: enableEncryptionAtHost is set to true'
  properties: {
    agentPoolProfiles: [
      {
        enableEncryptionAtHost: true
      }
    ]
  }
}
