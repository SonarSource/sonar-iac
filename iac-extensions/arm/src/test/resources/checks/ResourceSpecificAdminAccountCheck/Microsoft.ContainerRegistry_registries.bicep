resource acrAdminUserEnabled 'Microsoft.ContainerRegistry/registries@2021-09-01' = {
  name: 'acrAdminUserEnabled'
  properties: {
    adminUserEnabled: true // Noncompliant{{Make sure that enabling an administrative account or administrative permissions is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource acrAdminUserDisabled 'Microsoft.ContainerRegistry/registries@2021-09-01' = {
  name: 'acrAdminUserDisabled'
  properties: {
    adminUserEnabled: false
  }
}

resource acrAdminInvalidValue 'Microsoft.ContainerRegistry/registries@2021-09-01' = {
  name: 'acrAdminInvalidValue'
  properties: {
    adminUserEnabled: 'true'
  }
}

resource acrDefault 'Microsoft.ContainerRegistry/registries@2021-09-01' = {
  name: 'acrDefault'
  properties: {}
}
