resource compliant 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Compliant'
  properties: {
    encryptionState: 'Enabled'
  }
}

resource typeCompliant '${type}@2022-07-01' = {
  name: 'Compliant'
  properties: {
    infrastructureEncryption: 'Enabled'
  }
}

resource microsoftDataLakeStoreAccountsNonCompliantEncryptionIsExplicitlyDisabled 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Non-compliant: encryption is explicitly disabled'
  properties: {
    encryptionState: 'Disabled'
  }
}

resource microsoftDataLakeStoreAccountsNonCompliantEncryptionIsExplicitlyDisabled 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Non-compliant: encryption is explicitly disabled'
  properties: {
    encryptionState: null
  }
}

resource nonCompliantEncryptionPropertyIsNotSet 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Non-compliant: encryption property is not set'
}

resource typeNonCompliantEncryptionIsExplicitlyDisabled '${type}@2022-07-01' = {
  name: 'Non-compliant: encryption is explicitly disabled'
  properties: {
    infrastructureEncryption: 'Disabled'
  }
}

resource typeNonCompliantEncryptionPropertyIsNotSet '${type}@2022-07-01' = {
  name: 'Non-compliant: encryption property is not set'
}
