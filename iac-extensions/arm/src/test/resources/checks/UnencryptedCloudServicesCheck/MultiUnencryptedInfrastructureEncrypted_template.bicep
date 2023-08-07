resource compliant1 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Compliant'
  properties: {
    encryptionState: 'Enabled'
  }
}

resource compliant2 '${type}@2022-07-01' = {
  name: 'Compliant'
  properties: {
    infrastructureEncryption: 'Enabled'
  }
}

resource nonCompliant1 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Non-compliant: encryption is explicitly disabled'
  properties: {
    encryptionState: 'Disabled'
  }
}

resource nonCompliant2 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Non-compliant: encryption is explicitly disabled'
  properties: {
    encryptionState: null
  }
}

resource nonCompliant3 'Microsoft.DataLakeStore/accounts@2022-07-01' = {
  name: 'Non-compliant: encryption property is not set'
}

resource nonCompliant4 '${type}@2022-07-01' = {
  name: 'Non-compliant: encryption is explicitly disabled'
  properties: {
    infrastructureEncryption: 'Disabled'
  }
}

resource nonCompliant5 '${type}@2022-07-01' = {
  name: 'Non-compliant: encryption property is not set'
}
