resource compliant1 'Microsoft.ContainerService/managedClusters@2023-03-01' = {
  name: 'Compliant'
  properties: {
    aadProfile: {
      enableAzureRBAC: true
    }
    enableRBAC: true
  }
}

resource compliant2 'Microsoft.ContainerService/managedClusters@2023-03-01' = {
  name: 'Compliant: if RBAC-related properties are missing they\'re defaulting to true'
  properties: {}
}

resource noncompliant1 'Microsoft.ContainerService/managedClusters@2023-03-01' = {
  name: '2 issues: RBAC disabled in two locations'
  properties: {
    aadProfile: {
      enableAzureRBAC: false // Noncompliant{{Make sure that disabling role-based access control is safe here.}}
    }
    enableRBAC: false // Noncompliant{{Make sure that disabling role-based access control is safe here.}}
  }
}

resource noncompliant2 'Microsoft.ContainerService/managedClusters@2023-03-01' = {
  name: '1 issue: RBAC is disabled for AD integration only'
  properties: {
    aadProfile: {
      enableAzureRBAC: false // Noncompliant
    }
    enableRBAC: true
  }
}

resource noncompliant3 'Microsoft.ContainerService/managedClusters@2023-03-01' = {
  name: '1 issue: only k8s RBAC is disabled'
  properties: {
    aadProfile: {
      enableAzureRBAC: true
    }
    enableRBAC: false // Noncompliant
  }
}
