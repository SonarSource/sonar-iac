// Test for SONARIAC-2046: KeyVault accessPolicies empty array should not raise issue
// When using RBAC-based access control (enableRbacAuthorization: true),
// accessPolicies is commonly empty as RBAC replaces vault access policies

// Compliant - KeyVault with RBAC and empty accessPolicies
resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: 'compliantKeyVault'
  location: 'eastus'
  properties: {
    tenantId: subscription().tenantId
    sku: {
      family: 'A'
      name: 'standard'
    }
    enableRbacAuthorization: true
    accessPolicies: []
  }
}

// KeyVault without API version should raise issue (only @2023-07-01 is excepted)
resource keyVaultNoVersion 'Microsoft.KeyVault/vaults' = {
  name: 'noncompliantKeyVaultNoVersion'
  location: 'eastus'
  properties: {
    tenantId: subscription().tenantId
    sku: {
      family: 'A'
      name: 'standard'
    }
    enableRbacAuthorization: true
    accessPolicies: []  // Noncompliant {{Remove this empty array or complete with real code.}}
//  ^^^^^^^^^^^^^^^^^^
  }
}

// KeyVault with different API version should raise issue (only @2023-07-01 is excepted)
resource keyVault2 'Microsoft.KeyVault/vaults@2022-07-01' = {
  name: 'noncompliantKeyVault2'
  location: 'eastus'
  properties: {
    tenantId: subscription().tenantId
    sku: {
      family: 'A'
      name: 'standard'
    }
    enableRbacAuthorization: true
    accessPolicies: []  // Noncompliant {{Remove this empty array or complete with real code.}}
//  ^^^^^^^^^^^^^^^^^^
  }
}

// KeyVault with other empty properties should still raise issues
resource keyVault3 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: 'noncompliantKeyVault'
  location: 'eastus'
  properties: {
    tenantId: subscription().tenantId
    sku: {
      family: 'A'
      name: 'standard'
    }
    accessPolicies: []
    networkAcls: {}  // Noncompliant {{Remove this empty object or complete with real code.}}
//  ^^^^^^^^^^^^^^^
  }
}

// Other resource types should still raise issues for empty arrays
resource storageAccount 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: 'noncompliantStorage'
  location: 'eastus'
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    networkAcls: {
      ipRules: []  // Noncompliant {{Remove this empty array or complete with real code.}}
//    ^^^^^^^^^^^
    }
  }
}
