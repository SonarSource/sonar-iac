// Noncompliant@+1 {{Omitting "encryption.diskEncryptionSetId", "encryptionSettingsCollection" or "securityProfile.secureVMDiskEncryptionSetId" enables clear-text storage. Make sure it is safe here.}}
resource sensitiveDiskEncryptionSetIdEncryptionSettingsCollectionAndSecureVMDiskEncryptionSetIdAreNotSet 'Microsoft.Compute/disks@2022-07-02' = {
  name: 'Sensitive: diskEncryptionSetId, encryptionSettingsCollection and secureVMDiskEncryptionSetId are not set'
  properties: {
    encryption: {
      type: 'string'
    }
  }
}

resource sensitiveDiskEncryptionSetIdAndSecureVMDiskEncryptionSetIdAreNotSetEncryptionSettingsCollectionIsFalse 'Microsoft.Compute/disks@2022-07-02' = {
  name: 'Sensitive: diskEncryptionSetId, and secureVMDiskEncryptionSetId are not set, encryptionSettingsCollection is false'
  properties: {
    encryptionSettingsCollection: {
      // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
      enabled: false
    }
  }
}

resource compliantDiskEncryptionSetIdIsSet 'Microsoft.Compute/disks@2022-07-02' = {
  name: 'Compliant: diskEncryptionSetId is set'
  properties: {
    encryption: {
      diskEncryptionSetId: '123abc'
    }
  }
}

resource compliantEncryptionSettingsCollectionEnabledIsTrue 'Microsoft.Compute/disks@2022-07-02' = {
  name: 'Compliant: encryptionSettingsCollection/enabled is true'
  properties: {
    encryptionSettingsCollection: {
      enabled: true
    }
  }
}

resource compliantSecureVMDiskEncryptionSetIdIsSet 'Microsoft.Compute/disks@2022-07-02' = {
  name: 'Compliant: secureVMDiskEncryptionSetId is set'
  properties: {
    securityProfile: {
      secureVMDiskEncryptionSetId: 'abc123'
    }
  }
}

resource compliantEncryptionSettingsCollectionEnabledIsNotBooleanLiteral 'Microsoft.Compute/disks@2022-07-02' = {
  name: 'Compliant: encryptionSettingsCollection/enabled is not boolean literal'
  properties: {
    encryptionSettingsCollection: {
      enabled: {}
    }
  }
}

resource compliantDiskEncryptionSetIdEncryptionSettingsCollectionEnabledAndSecureVMDiskEncryptionSetIdIsSet 'Microsoft.Compute/disks@2022-07-02' = {
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
resource microsoftComputeSnapshotsSensitiveDiskEncryptionSetIdEncryptionSettingsCollectionAndSecureVMDiskEncryptionSetIdAreNotSet 'Microsoft.Compute/snapshots@2022-07-02' = {
  name: 'Sensitive: diskEncryptionSetId, encryptionSettingsCollection and secureVMDiskEncryptionSetId are not set'
  properties: {
    encryption: {}
  }
}

resource microsoftComputeSnapshotsSensitiveDiskEncryptionSetIdAndSecureVMDiskEncryptionSetIdAreNotSetEncryptionSettingsCollectionIsFalse 'Microsoft.Compute/snapshots@2022-07-02' = {
  name: 'Sensitive: diskEncryptionSetId, and secureVMDiskEncryptionSetId are not set, encryptionSettingsCollection is false'
  properties: {
    encryptionSettingsCollection: {
      // Noncompliant@+1 {{Make sure that using unencrypted cloud storage is safe here.}}
      enabled: false
    }
  }
}

resource microsoftComputeSnapshotsCompliantDiskEncryptionSetIdIsSet 'Microsoft.Compute/snapshots@2022-07-02' = {
  name: 'Compliant: diskEncryptionSetId is set'
  properties: {
    encryption: {
      diskEncryptionSetId: 'abc123'
    }
  }
}

resource compliantEncryptionSettingsCollectionIsSet 'Microsoft.Compute/snapshots@2022-07-02' = {
  name: 'Compliant: encryptionSettingsCollection is set'
  properties: {
    encryptionSettingsCollection: {
      enabled: true
    }
  }
}

resource compliantEncryptionSettingsCollectionIsNotBooleanLiteral 'Microsoft.Compute/snapshots@2022-07-02' = {
  name: 'Compliant: encryptionSettingsCollection is not boolean literal'
  properties: {
    encryptionSettingsCollection: {
      enabled: []
    }
  }
}

resource microsoftComputeSnapshotsCompliantSecureVMDiskEncryptionSetIdIsSet 'Microsoft.Compute/snapshots@2022-07-02' = {
  name: 'Compliant: secureVMDiskEncryptionSetId is set'
  properties: {
    securityProfile: {
      secureVMDiskEncryptionSetId: 'abc123'
    }
  }
}

resource compliantUnknownType 'unknown-type@2022-07-02' = {
  name: 'Compliant: unknown type'
  properties: {
    securityProfile: {}
  }
}

resource microsoftComputeSnapshotsCompliantDiskEncryptionSetIdEncryptionSettingsCollectionEnabledAndSecureVMDiskEncryptionSetIdIsSet 'Microsoft.Compute/snapshots@2022-07-02' = {
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
