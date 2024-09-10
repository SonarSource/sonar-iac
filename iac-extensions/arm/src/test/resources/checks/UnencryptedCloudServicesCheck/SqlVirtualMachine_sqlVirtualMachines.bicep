resource nonCompliant1 'Microsoft.SqlVirtualMachine/sqlVirtualMachines@2022-08-01-preview' = {
  name: 'Noncompliant: enableEncryption is set to false'
  properties: {
    autoBackupSettings: {
      // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
      enableEncryption: false
//    ^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource nonCompliant2 'Microsoft.SqlVirtualMachine/sqlVirtualMachines@2022-08-01-preview' = {
  name: 'Noncompliant: enableEncryption is missing'
  properties: {
    autoBackupSettings: {} // Noncompliant {{Omitting "enableEncryption" enables clear-text storage. Make sure it is safe here.}}
//                      ^^
  }
}

resource compliant_existing 'Microsoft.SqlVirtualMachine/sqlVirtualMachines@2022-08-01-preview' existing = {
  name: 'Compliant: existing'
}

resource nonCompliant3 'Microsoft.SqlVirtualMachine/sqlVirtualMachines@2022-08-01-preview' = {
  name: 'Noncompliant: enableEncryption is not set'
  properties: {
    autoBackupSettings: {
      enableEncryption: true
    }
  }
}
