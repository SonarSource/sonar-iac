// dataDisks ---

resource nonCompliantDataDisks 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Noncompliant dataDisks'
  properties: {
    virtualMachineProfile: {
      storageProfile: {
        dataDisks: [
          // Noncompliant@+1 {{Omitting "managedDisk.diskEncryptionSet.id" or "managedDisk.securityProfile.diskEncryptionSet.id" enables clear-text storage. Make sure it is safe here.}}
          {
  //      ^[el=+3;ec=11]
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
}

resource compliantDataDisks 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Compliant dataDisks'
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
}

// osDisk ---

resource nonCompliantOsDisk1 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Noncompliant osDisk 1'
  properties: {
    virtualMachineProfile: {
      storageProfile: {
        // Noncompliant@+1
        osDisk: {}
      }
    }
  }
}

resource nonCompliantOsDisk2 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Noncompliant osDisk 2'
  properties: {
    virtualMachineProfile: {
      storageProfile: {
        // Noncompliant@+1 {{Omitting "managedDisk.diskEncryptionSet.id" or "managedDisk.securityProfile.diskEncryptionSet.id" enables clear-text storage. Make sure it is safe here.}}
        osDisk: {
  //            ^[el=+3;ec=9]
          managedDisk: {}
        }
      }
    }
  }
}

resource nonCompliantOsDisk3 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Noncompliant osDisk 3'
  properties: {
    virtualMachineProfile: {
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
}

resource nonCompliantOsDisk4 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Noncompliant osDisk 4'
  properties: {
    virtualMachineProfile: {
      storageProfile: {
        // Noncompliant@+1 {{Omitting "managedDisk.diskEncryptionSet.id" or "managedDisk.securityProfile.diskEncryptionSet.id" enables clear-text storage. Make sure it is safe here.}}
        osDisk: {
  //            ^[el=+7;ec=9]
          managedDisk: {
            diskEncryptionSet: {
              id: ''
            }
          }
        }
      }
    }
  }
}

resource nonCompliantOsDisk5 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Noncompliant osDisk 5'
  properties: {
    virtualMachineProfile: {
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
}

resource nonCompliantOsDisk6 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Noncompliant osDisk 6'
  properties: {
    virtualMachineProfile: {
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
}

resource nonCompliantOsDisk7 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Noncompliant osDisk 7'
  properties: {
    virtualMachineProfile: {
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
}

resource compliantOsDisk1 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Compliant osDisk 1'
  properties: {
    virtualMachineProfile: {
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
}

resource compliantOsDisk2 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Compliant osDisk 2'
  properties: {
    virtualMachineProfile: {
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
}

resource compliantOsDisk3 'Microsoft.Compute/virtualMachineScaleSets@2022-07-01' = {
  name: 'Compliant osDisk 3'
  properties: {
    virtualMachineProfile: {
      storageProfile: {
        osDisk: {
          managedDisk: {
            diskEncryptionSet: {
              id: 'testId'
            }
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
}

// encryptionAtHost ---

resource nonCompliantEncryptionAtHost1 'Microsoft.Compute/virtualMachineScaleSets@2022-11-01' = {
  name: 'Noncompliant: encryptionAtHost is set to false'
  properties: {
    virtualMachineProfile: {
      securityProfile: {
        encryptionAtHost: false // Noncompliant {{Make sure that using unencrypted cloud storage is safe here.}}
      }
    }
  }
}

resource nonCompliantEncryptionAtHost2 'Microsoft.Compute/virtualMachineScaleSets@2022-11-01' = {
  name: 'Noncompliant: encryptionAtHost is missing'
  properties: {
    virtualMachineProfile: {
      securityProfile: {} // Noncompliant {{Omitting "encryptionAtHost" enables clear-text storage. Make sure it is safe here.}}
    }
  }
}

resource compliantEncryptionAtHost 'Microsoft.Compute/virtualMachineScaleSets@2022-11-01' = {
  name: 'Compliant: encryptionAtHost is set to true'
  properties: {
    virtualMachineProfile: {
      securityProfile: {
        encryptionAtHost: true
      }
    }
  }
}
