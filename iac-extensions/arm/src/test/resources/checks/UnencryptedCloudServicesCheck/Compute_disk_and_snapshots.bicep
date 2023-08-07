// Noncompliant@+1 {{Omitting "encryption.diskEncryptionSetId", "encryptionSettingsCollection" or "securityProfile.secureVMDiskEncryptionSetId" enables clear-text storage. Make sure it is safe here.}}
resource nonCompliant1 'Microsoft.Compute/disks@2022-07-02' = {
  name: 'Sensitive: diskEncryptionSetId, encryptionSettingsCollection and secureVMDiskEncryptionSetId are not set'
  properties: {
    encryption: {
      type: 'string'
    }
  }
}

resource nonCompliant2 'Microsoft.Compute/disks@2022-07-02' = {
  name: 'Sensitive: diskEncryptionSetId, and secureVMDiskEncryptionSetId are not set, encryptionSettingsCollection is false'
  properties: {
    encryptionSettingsCollection: {
      // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
      enabled: false
    }
  }
}

resource compliant 'Microsoft.Compute/disks@2022-07-02' = {
  name: 'Compliant: diskEncryptionSetId is set'
  properties: {
    encryption: {
      diskEncryptionSetId: '123abc'
    }
  }
}

resource compliant2 'Microsoft.Compute/disks@2022-07-02' = {
  name: 'Compliant: encryptionSettingsCollection/enabled is true'
  properties: {
    encryptionSettingsCollection: {
      enabled: true
    }
  }
}

resource compliant3 'Microsoft.Compute/disks@2022-07-02' = {
  name: 'Compliant: secureVMDiskEncryptionSetId is set'
  properties: {
    securityProfile: {
      secureVMDiskEncryptionSetId: 'abc123'
    }
  }
}

resource compliant4 'Microsoft.Compute/disks@2022-07-02' = {
  name: 'Compliant: encryptionSettingsCollection/enabled is not boolean literal'
  properties: {
    encryptionSettingsCollection: {
      enabled: {}
    }
  }
}

resource compliant5 'Microsoft.Compute/disks@2022-07-02' = {
  name: 'Compliant: diskEncryptionSetId, encryptionSettingsCollection/enabled and secureVMDiskEncryptionSetId is set'
  properties: {
    encryption: {
      diskEncryptionSetId: '123abc'
    }
    encryptionSettingsCollection: {
      enabled: true
    }
    securityProfile: {
      secureVMDiskEncryptionSetId: 'abc123'
    }
  }
}

// Noncompliant@+1 {{Omitting "encryption.diskEncryptionSetId", "encryptionSettingsCollection" or "securityProfile.secureVMDiskEncryptionSetId" enables clear-text storage. Make sure it is safe here.}}
resource nonCompliant3 'Microsoft.Compute/snapshots@2022-07-02' = {
  name: 'Sensitive: diskEncryptionSetId, encryptionSettingsCollection and secureVMDiskEncryptionSetId are not set'
  properties: {
    encryption: {}
  }
}

resource nonCompliant4 'Microsoft.Compute/snapshots@2022-07-02' = {
  name: 'Sensitive: diskEncryptionSetId, and secureVMDiskEncryptionSetId are not set, encryptionSettingsCollection is false'
  properties: {
    encryptionSettingsCollection: {
      // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
      enabled: false
    }
  }
}

resource compliant6 'Microsoft.Compute/snapshots@2022-07-02' = {
  name: 'Compliant: diskEncryptionSetId is set'
  properties: {
    encryption: {
      diskEncryptionSetId: 'abc123'
    }
  }
}

resource compliant7 'Microsoft.Compute/snapshots@2022-07-02' = {
  name: 'Compliant: encryptionSettingsCollection is set'
  properties: {
    encryptionSettingsCollection: {
      enabled: true
    }
  }
}

resource compliant8 'Microsoft.Compute/snapshots@2022-07-02' = {
  name: 'Compliant: encryptionSettingsCollection is not boolean literal'
  properties: {
    encryptionSettingsCollection: {
      enabled: []
    }
  }
}

resource compliant9 'Microsoft.Compute/snapshots@2022-07-02' = {
  name: 'Compliant: secureVMDiskEncryptionSetId is set'
  properties: {
    securityProfile: {
      secureVMDiskEncryptionSetId: 'abc123'
    }
  }
}

resource compliant10 'unknown-type@2022-07-02' = {
  name: 'Compliant: unknown type'
  properties: {
    securityProfile: {}
  }
}

resource compliant11 'Microsoft.Compute/snapshots@2022-07-02' = {
  name: 'Compliant: diskEncryptionSetId, encryptionSettingsCollection/enabled and secureVMDiskEncryptionSetId is set'
  properties: {
    encryption: {
      diskEncryptionSetId: '123abc'
    }
    encryptionSettingsCollection: {
      enabled: true
    }
    securityProfile: {
      secureVMDiskEncryptionSetId: 'abc123'
    }
  }
}
