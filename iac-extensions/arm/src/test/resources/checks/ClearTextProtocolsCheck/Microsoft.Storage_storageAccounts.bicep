resource Raise_issue_as_supportsHttpsTrafficOnly_is_set_to_false 'Microsoft.Storage/storageAccounts@2022-07-01' = {
  name: 'Raise issue as supportsHttpsTrafficOnly is set to false'
  properties: {
    supportsHttpsTrafficOnly: false // Noncompliant{{Make sure that using clear-text protocols is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource Compliant1 'Microsoft.Storage/storageAccounts@2022-07-01' = {
  name: 'Compliant1'
  properties: {}
}

resource Compliant2 'Microsoft.Storage/storageAccounts@2022-07-01' = {
  name: 'Compliant2'
  properties: {
    supportsHttpsTrafficOnly: true
  }
}

resource Compliant3 'Microsoft.Storage/storageAccounts@2022-07-01' = {
  name: 'Compliant3'
  properties: {
    supportsHttpsTrafficOnly: 'wrong format'
  }
}
