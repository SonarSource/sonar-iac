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

resource compliantVariableLocation 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: storageAccountName
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
}

resource compliantVariableLocation2 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: storageAccountName
  location: unresolved
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
}

resource compliantGlobalLocationLowercase 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: storageAccountName
  location: 'global'
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
}

resource compliantGlobalLocationUppercase 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: storageAccountName
  location: 'Global'
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
}

resource compliantExpressionLocation 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: storageAccountName
  location: resourceGroup().location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
}

param context object = {
  location: 'westus'
}
resource compliantObjectPropertyLocation 'Microsoft.Storage/storageAccounts@2022-09-01' = {
  name: storageAccountName
  location: context.location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
}
