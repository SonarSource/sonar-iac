// Noncompliant@+1 {{Make sure that authorizing potential anonymous access is safe here.}}
resource storageAccount 'Microsoft.Storage/storageAccounts@2022-09-01' = {
    name: 'example'
    properties: {}
}

resource storageAccount 'Microsoft.Storage/storageAccounts@2022-09-01' = {
    name: 'example'
    properties: {
        // Noncompliant@+1 {{Make sure that authorizing potential anonymous access is safe here.}}
        allowBlobPublicAccess: true
    }
}

resource storageAccount 'Microsoft.Storage/storageAccounts@2022-09-01' = {
    name: 'example'

    properties: {
        allowBlobPublicAccess: false
    }

    resource blobService 'blobServices@2022-09-01' = {
        name: 'default'
        resource containers 'containers@2022-09-01' = {
            name: 'exampleContainer'
            properties: {
                // Noncompliant@+1 {{Make sure that authorizing potential anonymous access is safe here.}}
                publicAccess: 'Blob'
            }
        }
    }
}

// Noncompliant@+1 {{Make sure that authorizing potential anonymous access is safe here.}}
resource storageAccount 'Microsoft.Storage/storageAccounts@2022-09-01' = {
    name: 'example'

    resource blobService 'blobServices@2022-09-01' = {
        name: 'default'
        resource containers 'containers@2022-09-01' = {
            name: 'exampleContainer'
            properties: {
                // Noncompliant@+1 {{Make sure that authorizing potential anonymous access is safe here.}}
                publicAccess: 'Blob'
            }
        }
    }
}

resource storageAccount 'Microsoft.Storage/storageAccounts/blobServices/containers@2022-09-01' = {
  name: 'example'
  properties: {
    allowBlobPublicAccess: false // Compliant
  }
}

// Noncompliant@+1 {{Make sure that authorizing potential anonymous access is safe here.}}
resource storageAccount 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: 'example'
  parent: storageAccount
  properties: {
    publicAccess: 'Blob'
  }
}

// Noncompliant@+1 {{Make sure that authorizing potential anonymous access is safe here.}}
resource storageAccount 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: 'example'
  properties: {
    publicAccess: 'Blob'
  }
}

resource storageAccount 'Microsoft.Storage/storageAccounts@2022-09-01' = {
    name: 'example'

    properties: {
      allowBlobPublicAccess: false // Compliant
    }

    resource blobService 'blobServices@2022-09-01' = {
        name: 'default'

        resource containers 'containers@2022-09-01' = {
            name: 'exampleContainer'
            properties: {
                publicAccess: 'None' // Compliant
            }
        }
    }
}

resource storageAccount 'Microsoft.Storage/storageAccounts@2022-09-01' = {
    name: 'example'

    properties: {
      allowBlobPublicAccess: false // Compliant
    }

    resource blobService 'blobServices@2022-09-01' = {
        name: 'default'

        resource containers 'containers@2022-09-01' = {
            name: 'exampleContainer'
            // Compliant - omitting `containers.publicAccess` defaults to \"None\"
        }
    }
}
