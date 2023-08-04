resource noncompliantEnableEncryptionIsSetToFalse 'Microsoft.SqlVirtualMachine/sqlVirtualMachines@2022-08-01-preview' = {
  name: 'Noncompliant: enableEncryption is set to false'
  properties: {
    autoBackupSettings: {
      // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
      enableEncryption: false
//    ^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource noncompliantEnableEncryptionIsMissing 'Microsoft.SqlVirtualMachine/sqlVirtualMachines@2022-08-01-preview' = {
  name: 'Noncompliant: enableEncryption is missing'
  properties: {
    autoBackupSettings: {} // Noncompliant {{Omitting "enableEncryption" enables clear-text storage. Make sure it is safe here.}}
//                      ^^
  }
}

resource noncompliantEnableEncryptionIsNotSet 'Microsoft.SqlVirtualMachine/sqlVirtualMachines@2022-08-01-preview' = {
  name: 'Noncompliant: enableEncryption is not set'
  properties: {
    autoBackupSettings: {
      enableEncryption: true
    }
  }
}
