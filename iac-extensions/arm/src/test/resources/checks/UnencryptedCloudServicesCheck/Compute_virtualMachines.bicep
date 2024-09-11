resource compliantExisting 'Microsoft.Compute/virtualMachines@2022-07-01' existing = {
  name: 'Compliant: existing'
}

// dataDisks ---

resource nonCompliantDataDisks 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant dataDisks'
  properties: {
    storageProfile: {
      dataDisks: [
        // Noncompliant@+1 {{Omitting "managedDisk.diskEncryptionSet.id" or "managedDisk.securityProfile.diskEncryptionSet.id" enables clear-text storage. Make sure it is safe here.}}
        {
//      ^[el=+3;ec=9]
          name: 'myDataDisk'
        }
        // Noncompliant@+1 {{Omitting "managedDisk.diskEncryptionSet.id" or "managedDisk.securityProfile.diskEncryptionSet.id" enables clear-text storage. Make sure it is safe here.}}
        {
          managedDisk: {}
        }
        // Noncompliant@+1
        {
          managedDisk: {
            diskEncryptionSet: {}
          }
        }
        // Noncompliant@+1 {{Omitting "managedDisk.diskEncryptionSet.id" or "managedDisk.securityProfile.diskEncryptionSet.id" enables clear-text storage. Make sure it is safe here.}}
        {
          managedDisk: {
            diskEncryptionSet: {
              id: ''
            }
          }
        }
        // Noncompliant@+1
        {
          managedDisk: {
            securityProfile: {
              diskEncryptionSet: {}
            }
          }
        }
        // Noncompliant@+1 {{Omitting "managedDisk.diskEncryptionSet.id" or "managedDisk.securityProfile.diskEncryptionSet.id" enables clear-text storage. Make sure it is safe here.}}
        {
          managedDisk: {
            securityProfile: {
              diskEncryptionSet: {
                id: ''
              }
            }
          }
        }
        // Noncompliant@+1
        {
          anotherKindOfDisk: {}
        }
      ]
    }
  }
}

resource compliantDataDisks 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Compliant dataDisks'
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
          managedDisk: {
            securityProfile : {
              diskEncryptionSet: {
                id: 'testId'
              }
            }
          }
        }
        {
          managedDisk: {
            diskEncryptionSet: {
              id: 'testId'
            }
            securityProfile : {
              diskEncryptionSet: {
                id: ''
              }
            }
          }
        }
        {
          managedDisk: {
            diskEncryptionSet: {
              id: ''
            }
            securityProfile : {
              diskEncryptionSet: {
                id: 'testId'
              }
            }
          }
        }
        {
          managedDisk: {
            diskEncryptionSet: {
              id: 'testId1'
            }
            securityProfile : {
              diskEncryptionSet: {
                id: 'testId2'
              }
            }
          }
        }
      ]
    }
  }
}

// osDisk ---

resource nonCompliantOsDisk1 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant osDisk 1'
  properties: {
    storageProfile: {
      // Noncompliant@+1
      osDisk: {}
    }
  }
}

resource nonCompliantOsDisk2 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant osDisk 2'
  properties: {
    storageProfile: {
      osDisk: {
        encryptionSettings: false // Noncompliant {{Make sure that using unencrypted cloud storage is safe here.}}
      }
    }
  }
}

resource nonCompliantOsDisk3 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant osDisk 3'
  properties: {
    storageProfile: {
      osDisk: {
        encryptionSettings: {} // Noncompliant {{Make sure that using unencrypted cloud storage is safe here.}}
      }
    }
  }
}

resource nonCompliantOsDisk4 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant osDisk 4'
  properties: {
    storageProfile: {
      osDisk: {
        encryptionSettings: {
          enabled: false // Noncompliant {{Make sure that using unencrypted cloud storage is safe here.}}
        }
      }
    }
  }
}

resource nonCompliantOsDisk5 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant osDisk 5'
  properties: {
    storageProfile: {
      // Noncompliant@+1 {{Omitting "encryptionSettings", "managedDisk.diskEncryptionSet.id" or "managedDisk.securityProfile.diskEncryptionSet.id" enables clear-text storage. Make sure it is safe here.}}
      osDisk: {
//            ^[el=+3;ec=7]
        managedDisk: {}
      }
    }
  }
}

resource nonCompliantOsDisk6 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant osDisk 6'
  properties: {
    storageProfile: {
      // Noncompliant@+1
      osDisk: {
        managedDisk: {
          diskEncryptionSet: {}
        }
      }
    }
  }
}

resource nonCompliantOsDisk7 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant osDisk 7'
  properties: {
    storageProfile: {
      // Noncompliant@+1 {{Omitting "encryptionSettings", "managedDisk.diskEncryptionSet.id" or "managedDisk.securityProfile.diskEncryptionSet.id" enables clear-text storage. Make sure it is safe here.}}
      osDisk: {
//            ^[el=+7;ec=7]
        managedDisk: {
          diskEncryptionSet: {
            id: ''
          }
        }
      }
    }
  }
}

resource nonCompliantOsDisk8 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant osDisk 8'
  properties: {
    storageProfile: {
      // Noncompliant@+1
      osDisk: {
        managedDisk: {
          securityProfile: {}
        }
      }
    }
  }
}

resource nonCompliantOsDisk9 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant osDisk 9'
  properties: {
    storageProfile: {
      // Noncompliant@+1
      osDisk: {
        managedDisk: {
          securityProfile: {
            diskEncryptionSet: {}
          }
        }
      }
    }
  }
}

resource nonCompliantOsDisk10 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant osDisk 10'
  properties: {
    storageProfile: {
      // Noncompliant@+1
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

resource nonCompliantOsDisk11 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Noncompliant osDisk 11'
  properties: {
    storageProfile: {
      osDisk: {
        encryptionSettings: false // Noncompliant {{Make sure that using unencrypted cloud storage is safe here.}}
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

resource compliantOsDisk1 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Compliant osDisk 1'
  properties: {
    storageProfile: {
      osDisk: {
        encryptionSettings: true
      }
    }
  }
}

resource compliantOsDisk2 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Compliant osDisk 2'
  properties: {
    storageProfile: {
      osDisk: {
        encryptionSettings: {
          enabled: true
        }
      }
    }
  }
}

resource compliantOsDisk3 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Compliant osDisk 3'
  properties: {
    storageProfile: {
      osDisk: {
        managedDisk: {
          diskEncryptionSet: {
            id: 'testId'
          }
        }
      }
    }
  }
}

resource compliantOsDisk4 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Compliant osDisk 4'
  properties: {
    storageProfile: {
      osDisk: {
        managedDisk: {
          securityProfile: {
            diskEncryptionSet: {
              id: 'testId'
            }
          }
        }
      }
    }
  }
}

resource compliantOsDisk5 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Compliant osDisk 5'
  properties: {
    storageProfile: {
      osDisk: {
        encryptionSettings: false
        managedDisk: {
          diskEncryptionSet: {
            id: 'testId'
          }
        }
      }
    }
  }
}

resource compliantOsDisk6 'Microsoft.Compute/virtualMachines@2022-07-01' = {
  name: 'Compliant osDisk 6'
  properties: {
    storageProfile: {
      osDisk: {
        encryptionSettings: false
        managedDisk: {
          securityProfile: {
            diskEncryptionSet: {
              id: 'testId'
            }
          }
        }
      }
    }
  }
}

// encryptionAtHost ---

resource nonCompliantEncryptionAtHost1 'Microsoft.Compute/virtualMachines@2022-11-01' = {
  name: 'Noncompliant: encryptionAtHost is set to false'
  properties: {
    securityProfile: {
      encryptionAtHost: false // Noncompliant {{Make sure that using unencrypted cloud storage is safe here.}}
    }
  }
}

resource nonCompliantEncryptionAtHost2 'Microsoft.Compute/virtualMachines@2022-11-01' = {
  name: 'Noncompliant: encryptionAtHost is missing'
  properties: {
    securityProfile: {} // Noncompliant {{Omitting "encryptionAtHost" enables clear-text storage. Make sure it is safe here.}}
  }
}

resource compliantEncryptionAtHost 'Microsoft.Compute/virtualMachines@2022-11-01' = {
  name: 'Compliant: encryptionAtHost is set to true'
  properties: {
    securityProfile: {
      encryptionAtHost: true
    }
  }
}
