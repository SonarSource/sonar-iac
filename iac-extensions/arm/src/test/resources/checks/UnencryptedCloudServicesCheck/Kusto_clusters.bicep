resource nonCompliant1 'Microsoft.Kusto/clusters@2022-12-29' = {
  name: 'Noncompliant: enableDiskEncryption is set to false'
  properties: {
    // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
    enableDiskEncryption: false
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

// Noncompliant@+1 {{Omitting "enableDiskEncryption" enables clear-text storage. Make sure it is safe here.}}
resource nonCompliant2 'Microsoft.Kusto/clusters@2022-12-29' = {
  name: 'Noncompliant: enableDiskEncryption is missing'
  properties: {}
}

resource compliant1 'Microsoft.Kusto/clusters@2022-12-29' = {
  name: 'Compliant: enableDiskEncryption is set to true'
  properties: {
    enableDiskEncryption: true
  }
}
