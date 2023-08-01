resource nonCompliant1 'Microsoft.Batch/batchAccounts/pools@2022-10-01' = {
  name: 'nonCompliant1'
  properties: {
    startTask: {
      userIdentity: {
        autoUser: {
          elevationLevel: 'Admin' // Noncompliant {{Make sure that enabling an administrative account or administrative permissions is safe here.}}
//        ^^^^^^^^^^^^^^^^^^^^^^^
        }
      }
    }
  }
}

resource compliant1 'Microsoft.Batch/batchAccounts/pools@2022-10-01' = {
  name: 'compliant1'
  properties: {
    startTask: {
      userIdentity: {
        autoUser: {
          elevationLevel: 'NonAdmin'
        }
      }
    }
  }
}

resource compliant2 'Microsoft.Batch/batchAccounts/pools@2022-10-01' = {
  name: 'compliant2'
  properties: {
    startTask: {
      userIdentity: {
        autoUser: {}
      }
    }
  }
}
