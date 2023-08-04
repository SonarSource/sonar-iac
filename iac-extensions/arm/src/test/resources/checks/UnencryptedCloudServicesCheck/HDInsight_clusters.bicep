resource noncompliantEncryptDataDisksIsSetToFalse 'Microsoft.HDInsight/clusters@2021-06-01' = {
  name: 'Noncompliant: encryptDataDisks is set to false'
  properties: {
    computeProfile: {
      roles: [
        {
          // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
          encryptDataDisks: false
        }
      ]
    }
  }
}

resource noncompliantEncryptDataDisksIsMissing 'Microsoft.HDInsight/clusters@2021-06-01' = {
  name: 'Noncompliant: encryptDataDisks is missing'
  properties: {
    computeProfile: {
      roles: [
        {}  // Noncompliant {{Omitting "encryptDataDisks" enables clear-text storage. Make sure it is safe here.}}
      ]
    }
  }
}

resource compliantEncryptDataDisksIsSetToTrue 'Microsoft.HDInsight/clusters@2021-06-01' = {
  name: 'Compliant: encryptDataDisks is set to true'
  properties: {
    computeProfile: {
      roles: [
        {
          encryptDataDisks: true
        }
      ]
    }
  }
}

resource noncompliantEncryptionAtHostIsSetToFalse 'Microsoft.HDInsight/clusters@2021-06-01' = {
  name: 'Noncompliant: encryptionAtHost is set to false'
  properties: {
    diskEncryptionProperties: {
      encryptionAtHost: false  // Noncompliant {{Make sure that using unencrypted cloud storage is safe here.}}
    }
  }
}

resource noncompliantEncryptionAtHostIsMissing 'Microsoft.HDInsight/clusters@2021-06-01' = {
  name: 'Noncompliant: encryptionAtHost is missing'
  properties: {
    diskEncryptionProperties: {}  // Noncompliant {{Omitting "encryptionAtHost" enables clear-text storage. Make sure it is safe here.}}
  }
}

resource compliantEncryptionAtHostIsSetToTrue 'Microsoft.HDInsight/clusters@2021-06-01' = {
  name: 'Compliant: encryptionAtHost is set to true'
  properties: {
    diskEncryptionProperties: {
      encryptionAtHost: true
    }
  }
}
