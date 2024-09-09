resource nonCompliant1 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    storageProfile: {
      dataDisks: [
        {
          // Noncompliant@+1 {{Omitting "diskEncryptionSet" enables clear-text storage. Make sure it is safe here.}}
          managedDisk: {}
//                     ^^
        }
        {
          managedDisk: {
            diskEncryptionSet: {
              id: '' // Noncompliant {{Omitting "id" enables clear-text storage. Make sure it is safe here.}}
//            ^^^^^^
            }
          }
        }
        {
          managedDisk: {
            diskEncryptionSet: {}  // Noncompliant {{Omitting "id" enables clear-text storage. Make sure it is safe here.}}
//                             ^^
          }
        }
      ]
      // Noncompliant@+1 {{Omitting "encryptionSettings" enables clear-text storage. Make sure it is safe here.}}
      osDisk: {
//            ^[el=+5;ec=7]
        managedDisk: {
          securityProfile: {}
        }
      }
    }
  }
}

resource nonCompliant2 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    storageProfile: {
      osDisk: {
        encryptionSettings: false // Noncompliant {{Make sure that using unencrypted cloud storage is safe here.}}
      }
    }
  }
}

resource nonCompliant3 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    storageProfile: {
      // Noncompliant@+1 {{Omitting "encryptionSettings" enables clear-text storage. Make sure it is safe here.}}
      osDisk: {}
    }
  }
}

resource nonCompliant3 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    storageProfile: {
      // Noncompliant@+1 {{Omitting "encryptionSettings" enables clear-text storage. Make sure it is safe here.}}
      osDisk: {
          managedDisk: {}
      }
    }
  }
}

resource nonCompliant3 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    storageProfile: {
      // Noncompliant@+1 {{Omitting "encryptionSettings" enables clear-text storage. Make sure it is safe here.}}
      osDisk: {
          managedDisk: {
              diskEncryptionSet: {}
          }
      }
    }
  }
}

resource nonCompliant3 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    storageProfile: {
      // Noncompliant@+1 {{Omitting "encryptionSettings" enables clear-text storage. Make sure it is safe here.}}
      osDisk: {
          managedDisk: {
              diskEncryptionSet: {
                  id: ''
              }
          }
      }
    }
  }
}

resource nonCompliant3 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    storageProfile: {
      // Noncompliant@+1 {{Omitting "encryptionSettings" enables clear-text storage. Make sure it is safe here.}}
      osDisk: {
          managedDisk: {
              securityProfile: {
                  diskEncryptionSet: {
                      id: ''
                  }
              }
          }
      }
    }
  }
}

resource compliant 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Compliant'
  properties: {
    storageProfile: {
      dataDisks: [
        {
          managedDisk: {
            diskEncryptionSet: {
              id: 'testId'
            }
          }
        }
        {
          anotherKindOfDisk: {}
        }
      ]
    }
  }
}

resource nonCompliant3 'Microsoft.Compute/virtualMachines@2022-11-01' = {
  name: 'Noncompliant: encryptionAtHost is set to false'
  properties: {
    securityProfile: {
      encryptionAtHost: false // Noncompliant {{Make sure that using unencrypted cloud storage is safe here.}}
    }
  }
}

resource nonCompliant4 'Microsoft.Compute/virtualMachines@2022-11-01' = {
  name: 'Noncompliant: encryptionAtHost is missing'
  properties: {
    securityProfile: {} // Noncompliant {{Omitting "encryptionAtHost" enables clear-text storage. Make sure it is safe here.}}
  }
}

resource compliant2 'Microsoft.Compute/virtualMachines@2022-11-01' = {
  name: 'Compliant: encryptionAtHost is set to true'
  properties: {
    securityProfile: {
      encryptionAtHost: true
    }
  }
}
