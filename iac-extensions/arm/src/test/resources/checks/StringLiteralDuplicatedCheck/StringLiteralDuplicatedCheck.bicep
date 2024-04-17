var storageAccountName = 'app Super Storage 2'

resource storageAccount 'Microsoft.Storage/storageAccounts@2021-01-01' = {
  name: 'appSuperStorage'
  tags: {
    displayName: 'appSuperStorage'
    shortName: 'appSuperStorage'
  }
}

resource storageAccount 'Microsoft.Storage/storageAccounts@2021-01-01' = {
  name: 'app Super Storage' // Noncompliant {{Define a variable instead of duplicating this literal "app Super Storage" 3 times.}}
//      ^^^^^^^^^^^^^^^^^^^
  tags: {
    displayName: 'app Super Storage'
//               ^^^^^^^^^^^^^^^^^^^< {{Duplication.}}
    shortName: 'app Super Storage'
//             ^^^^^^^^^^^^^^^^^^^< {{Duplication.}}
  }
}

resource storageAccount 'Microsoft.Storage/storageAccounts@2021-01-01' = {
  name: storageAccountName
  tags: {
    displayName: storageAccountName
    shortName: storageAccountName
  }
}

resource storageAccount 'Microsoft.Storage/storageAccounts' = {
  name: storageAccountName
  tags: {
    displayName: 'Microsoft.Storage/storageAccounts'
    shortName: 'Microsoft.Storage/storageAccounts'
  }
}

resource storageAccount 'Microsoft.Storage/foo@' = { // invalid, but to avoid crashes let's test this as well
  name: storageAccountName
}


