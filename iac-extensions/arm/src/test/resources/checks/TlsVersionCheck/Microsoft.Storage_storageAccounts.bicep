resource nonCompliant1 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: 'Raise an issue: older TLS versions shouldn\'t be allowed'
  properties: {
    // Noncompliant@+1 {{Change this code to disable support of older TLS versions.}}
    minimumTlsVersion: 'TLS1_0'
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

// Noncompliant@+1 {{Set minimumTlsVersion/minimalTlsVersion to disable support of older TLS versions.}}
resource nonCompliant2 'Microsoft.Storage/storageAccounts@2022-09-01' = {
//                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'Raise an issue: TLS version is absent'
}

resource compliant 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: 'Compliant'
  properties: {
    minimumTlsVersion: 'TLS1_2'
  }
}
