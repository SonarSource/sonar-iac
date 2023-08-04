// Noncompliant@+1 {{Omitting "managedDiskCustomerKeyUri" enables clear-text storage. Make sure it is safe here.}}
resource microsoftDocumentDBCassandraClustersDataCentersNoncompliant 'Microsoft.DocumentDB/cassandraClusters/dataCenters@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    backupStorageCustomerKeyUri: 'uri'
  }
}

// Noncompliant@+1 {{Omitting "backupStorageCustomerKeyUri" enables clear-text storage. Make sure it is safe here.}}
resource microsoftDocumentDBCassandraClustersDataCentersNoncompliant 'Microsoft.DocumentDB/cassandraClusters/dataCenters@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    managedDiskCustomerKeyUri: 'uri'
  }
}

resource compliant 'Microsoft.DocumentDB/cassandraClusters/dataCenters@2022-07-01' = {
  name: 'Compliant'
  properties: {
    backupStorageCustomerKeyUri: 'uri'
    managedDiskCustomerKeyUri: 'uri'
  }
}

// Noncompliant@+1 {{Omitting "diskEncryptionSetID" enables clear-text storage. Make sure it is safe here.}}
resource microsoftContainerServiceManagedClustersNoncompliant 'Microsoft.ContainerService/managedClusters@2022-07-01' = {
  name: 'Noncompliant'
  properties: {}
}

resource microsoftContainerServiceManagedClustersCompliant 'Microsoft.ContainerService/managedClusters@2022-07-01' = {
  name: 'Compliant'
  properties: {
    diskEncryptionSetID: 'id'
  }
}

resource microsoftRedHatOpenShiftOpenShiftClustersNoncompliant 'Microsoft.RedHatOpenShift/openShiftClusters@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    // Noncompliant@+1 {{Omitting "encryptionAtHost" enables clear-text storage. Make sure it is safe here.}}
    masterProfile: {
//                 ^[el=+3;ec=5]
      diskEncryptionSetId: 'id'
    }
    workerProfiles: [
      { // Noncompliant {{Omitting "encryptionAtHost" enables clear-text storage. Make sure it is safe here.}}
//    ^[el=+3;ec=7]
        diskEncryptionSetId: 'id'
      }
    ]
  }
}

resource noncompliant2 'Microsoft.RedHatOpenShift/openShiftClusters@2022-07-01' = {
  name: 'Noncompliant2'
  properties: {
    masterProfile: { // Noncompliant {{Omitting "diskEncryptionSetId" enables clear-text storage. Make sure it is safe here.}}
//                 ^[el=+3;ec=5]
      encryptionAtHost: 'Enabled'
    }
    workerProfiles: [
      { // Noncompliant {{Omitting "diskEncryptionSetId" enables clear-text storage. Make sure it is safe here.}}
//    ^[el=+3;ec=7]
        encryptionAtHost: 'Enabled'
      }
    ]
  }
}

resource microsoftRedHatOpenShiftOpenShiftClustersCompliant 'Microsoft.RedHatOpenShift/openShiftClusters@2022-07-01' = {
  name: 'Compliant'
  properties: {
    masterProfile: {
      diskEncryptionSetId: 'id'
      encryptionAtHost: 'Enabled'
    }
    workerProfiles: [
      {
        diskEncryptionSetId: 'id'
        encryptionAtHost: 'Enabled'
      }
    ]
  }
}
