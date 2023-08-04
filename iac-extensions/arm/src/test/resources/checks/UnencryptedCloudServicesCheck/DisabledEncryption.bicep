resource noncompliant 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
    encryptionState: 'Disabled'
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource noncompliant2 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Noncompliant2'
  properties: {
    // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
    encryptionState: null
//  ^^^^^^^^^^^^^^^^^^^^^
  }
}

// Noncompliant@+1 {{Omitting "encryptionState" enables clear-text storage. Make sure it is safe here.}}
resource noncompliant3 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Noncompliant3'
  properties: {}
}

resource microsoftDBforMySQLServersNoncompliant 'Microsoft.DBforMySQL/servers@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
    infrastructureEncryption: 'Disabled'
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource microsoftDBforPostgreSQLServersNoncompliant2 'Microsoft.DBforPostgreSQL/servers@2022-07-01' = {
  name: 'Noncompliant2'
  properties: {
    // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
    infrastructureEncryption: 'Disabled'
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource microsoftRecoveryServicesVaultsNoncompliant 'Microsoft.RecoveryServices/vaults@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    encryption: {
      // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
      infrastructureEncryption: 'Disabled'
    }
  }
}

resource microsoftRecoveryServicesVaultsBackupEncryptionConfigsNoncompliant 'Microsoft.RecoveryServices/vaults/backupEncryptionConfigs@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
    infrastructureEncryptionState: 'Disabled'
  }
}

resource microsoftRecoveryServicesVaultsBackupEncryptionConfigsNoncompliant2 'Microsoft.RecoveryServices/vaults/backupEncryptionConfigs@2022-07-01' = {
  name: 'Noncompliant2'
  properties: {
    infrastructureEncryptionState: 'Invalid'
  }
}

resource compliant 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Compliant'
  properties: {
    encryptionState: 'Enabled'
  }
}

resource compliant2 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Compliant2'
  properties: {
    encryptionState: 'Unknown'
  }
}

resource compliant3 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Compliant3'
  properties: {
    encryptionState: false
  }
}
