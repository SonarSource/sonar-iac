resource acrAdminUserEnabled 'Microsoft.ContainerRegistry/registries@2021-09-01' = {
  properties: {
    adminUserEnabled: true // Noncompliant{{Make sure that enabling an administrative account or administrative permissions is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource acrAdminUserDisabled 'Microsoft.ContainerRegistry/registries@2021-09-01' = {
  properties: {
    adminUserEnabled: false
  }
}

resource acrAdminInvalidValue 'Microsoft.ContainerRegistry/registries@2021-09-01' = {
  properties: {
    adminUserEnabled: 'true'
  }
}

resource acrDefault 'Microsoft.ContainerRegistry/registries@2021-09-01' = {
  properties: {}
}
