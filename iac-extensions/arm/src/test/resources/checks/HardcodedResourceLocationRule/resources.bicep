resource nonCompliant1 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: storageAccountName
  location: 'westus'  // Noncompliant{{Replace this hardcoded location with a parameter.}}
//          ^^^^^^^^
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
}

resource nonCompliant2 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: storageAccountName
  location: '${region}us' // Noncompliant
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
}

param location string = resourceGroup().location

resource compliant1 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: storageAccountName
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
}

resource compliant2 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: storageAccountName
  location: unresolved
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
}
