resource nonCompliant1 'Microsoft.HDInsight/clusters/applications@2021-06-01' = {
  name: 'Noncompliant: encryptDataDisks is set to false'
  properties: {
    computeProfile: {
      roles: [
        {
          // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
          encryptDataDisks: false
//        ^^^^^^^^^^^^^^^^^^^^^^^
        }
      ]
    }
  }
}

resource nonCompliant2 'Microsoft.HDInsight/clusters/applications@2021-06-01' = {
  name: 'Noncompliant: encryptDataDisks is missing'
  properties: {
    computeProfile: {
      roles: [
        {} // Noncompliant {{Omitting "encryptDataDisks" enables clear-text storage. Make sure it is safe here.}}
//      ^^
      ]
    }
  }
}

resource compliant1 'Microsoft.HDInsight/clusters/applications@2021-06-01' = {
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
