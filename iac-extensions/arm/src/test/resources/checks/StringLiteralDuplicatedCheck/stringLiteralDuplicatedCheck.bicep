resource storageAccount1 'Microsoft.Storage/storageAccounts@2021-01-01' = {
  name: 'appSuperStorage'  // Noncompliant {{Define a constant instead of duplicating this literal "appSuperStorage" 3 times.}}
//      ^^^^^^^^^^^^^^^^^
  tags: {
    displayName: 'appSuperStorage'
//               ^^^^^^^^^^^^^^^^^< {{Duplication.}}
    shortName: 'appSuperStorage'
//             ^^^^^^^^^^^^^^^^^< {{Duplication.}}
  }
}

resource storageAccount2 'Microsoft.Storage/storageAccounts@2021-01-01' = {
  name: 'test_duplicated'  // Noncompliant {{Define a constant instead of duplicating this literal "test_duplicated" 3 times.}}
//      ^^^^^^^^^^^^^^^^^
  tags: {
    displayName: 'test_duplicated'
//               ^^^^^^^^^^^^^^^^^< {{Duplication.}}
    shortName: 'test_duplicated'
//             ^^^^^^^^^^^^^^^^^< {{Duplication.}}
    not_reported: 'test_duplicated' // This one is currently not reported: we only report the N first duplication
  }
}

// The resource type and version are not reported as they cannot be replaced by variables
resource storageAccount3 'Microsoft.Storage/storageAccounts@2021-01-01' = {
  name: 'smal'
  tags: {
    displayName: 'smal'
    shortName: 'smal'
  }
}

// Interpolated strings are not considered duplicate
resource storageAccount4 'Microsoft.Storage/storageAccounts@2021-01-01' = {
  name: 'a${123}b'
  tags: {
    displayName: 'a${123}b'
    shortName: 'a${123}b'
  }
}
