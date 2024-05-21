var storageAccountName = 'app Super Storage 2'

resource storageAccount 'Microsoft.Storage/storageAccounts@2021-01-01' = {
  name: 'appSuperStorage'
  tags: {
    displayName: 'appSuperStorage'
    shortName: 'appSuperStorage'
  }
}

resource storageAccount 'Microsoft.Storage/storageAccounts@2021-01-01' = {
  name: 'app Super Storage' // Noncompliant {{Define a variable instead of duplicating this literal "app Super Storage" 5 times.}}
//      ^^^^^^^^^^^^^^^^^^^
  tags: {
    displayName: 'app Super Storage'
//               ^^^^^^^^^^^^^^^^^^^< {{Duplication.}}
    shortName: 'app Super Storage'
//             ^^^^^^^^^^^^^^^^^^^< {{Duplication.}}
    someName: 'app Super Storage'
//            ^^^^^^^^^^^^^^^^^^^< {{Duplication.}}
    yetAnotherName: 'app Super Storage'
//                  ^^^^^^^^^^^^^^^^^^^< {{Duplication.}}
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

resource storageAccount 'Microsoft.Storage/storageAccounts' = {
  name: storageAccountName
  tags: {
    tag1: '1-0-0'
    tag2: '1-0-0'
    tag3: '1-0-0'
    tag4: '1-0-0'
    tag5: '1-0-0'
  }
}

// Compliant: module paths cannot be variables
module moduleName_one '../dosomething.bicep' = {
  name: 'deployName_one'
  params: {
    argument: 'test1'
  }
}
module moduleName_two '../dosomething.bicep' = {
  name: 'deployName_two'
  params: {
    argument: 'test2'
  }
}
module moduleName_tree '../dosomething.bicep' = {
  name: 'deployName_tree'
  params: {
    argument: 'test3'
  }
}
module moduleName_four '../dosomething.bicep' = {
  name: 'deployName_four'
  params: {
    argument: 'test4'
  }
}
module moduleName_five '../dosomething.bicep' = {
  name: 'deployName_five'
  params: {
    argument: 'test5'
  }
}
module moduleName_five '../dosomething.bicep' = {
  name: 'deployName_six'
  params: {
    argument: 'test6'
  }
}
