resource noncompliant 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Noncompliant'
  properties: {
    virtualMachineProfile: {
      storageProfile: {
        dataDisks: [
          {
            managedDisk: {} // Noncompliant {{Omitting "diskEncryptionSet" enables clear-text storage. Make sure it is safe here.}}
          }
          {
            managedDisk: {
              diskEncryptionSet: {
                id: '' // Noncompliant {{Omitting "id" enables clear-text storage. Make sure it is safe here.}}
              }
            }
          }
          {
            managedDisk: {
              diskEncryptionSet: {} // Noncompliant {{Omitting "id" enables clear-text storage. Make sure it is safe here.}}
            }
          }
          {
            managedDisk: { // Noncompliant {{Omitting "diskEncryptionSet" enables clear-text storage. Make sure it is safe here.}}
//                       ^[el=+7;ec=13]
              securityProfile: {
                diskEncryptionSet: {
                  id: '' // Noncompliant {{Omitting "id" enables clear-text storage. Make sure it is safe here.}}
                }
              }
            }
          }
        ]
        osDisk: {
          managedDisk: { // Noncompliant {{Omitting "diskEncryptionSet" enables clear-text storage. Make sure it is safe here.}}
//                     ^[el=+3;ec=11]
            securityProfile: {} // Noncompliant {{Omitting "diskEncryptionSet" enables clear-text storage. Make sure it is safe here.}}
          }
          securityProfile: {}
        }
      }
    }
  }
}

resource compliant 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Compliant'
  properties: {
    virtualMachineProfile: {
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
}

resource noncompliantEncryptionAtHostIsSetToFalse 'Microsoft.Compute/virtualMachineScaleSets@2022-11-01' = {
  name: 'Noncompliant: encryptionAtHost is set to false'
  properties: {
    virtualMachineProfile: {
      securityProfile: {
        encryptionAtHost: false // Noncompliant {{Make sure that using unencrypted cloud storage is safe here.}}
      }
    }
  }
}

resource noncompliantEncryptionAtHostIsMissing 'Microsoft.Compute/virtualMachineScaleSets@2022-11-01' = {
  name: 'Noncompliant: encryptionAtHost is missing'
  properties: {
    virtualMachineProfile: {
      securityProfile: {} // Noncompliant {{Omitting "encryptionAtHost" enables clear-text storage. Make sure it is safe here.}}
    }
  }
}

resource compliantEncryptionAtHostIsSetToTrue 'Microsoft.Compute/virtualMachineScaleSets@2022-11-01' = {
  name: 'Compliant: encryptionAtHost is set to true'
  properties: {
    virtualMachineProfile: {
      securityProfile: {
        encryptionAtHost: true
      }
    }
  }
}
