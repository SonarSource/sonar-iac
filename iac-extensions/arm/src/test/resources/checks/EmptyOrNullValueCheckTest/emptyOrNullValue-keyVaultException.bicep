// Test for SONARIAC-2046: KeyVault accessPolicies empty array should not raise issue
// When using RBAC-based access control (enableRbacAuthorization: true),
// accessPolicies is commonly empty as RBAC replaces vault access policies

param location string = 'eastus'

// Compliant - KeyVault with RBAC and empty accessPolicies
resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: 'compliantKeyVault'
  location: location
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

// KeyVault without API version - exemption applies to all API versions (SONARIAC-2688)
// accessPolicies: [] exemption for this API version is tested via inline content in shouldAllowKeyVaultEmptyAccessPoliciesAllVersionsBicep
resource keyVaultNoVersion 'Microsoft.KeyVault/vaults' = {
  name: 'compliantKeyVaultNoVersion'
  location: location
  properties: {
    tenantId: subscription().tenantId
    sku: {
      family: 'A'
      name: 'standard'
    }
    enableRbacAuthorization: true
  }
}

// KeyVault with different API version - exemption applies to all API versions (SONARIAC-2688)
// accessPolicies: [] exemption for this API version is tested via inline content in shouldAllowKeyVaultEmptyAccessPoliciesAllVersionsBicep
resource keyVault2 'Microsoft.KeyVault/vaults@2022-07-01' = {
  name: 'compliantKeyVault2'
  location: location
  properties: {
    tenantId: subscription().tenantId
    sku: {
      family: 'A'
      name: 'standard'
    }
    enableRbacAuthorization: true
  }
}

// KeyVault with preview API version - exemption applies to all API versions (SONARIAC-2688, original reported FP)
// accessPolicies: [] exemption for this API version is tested via inline content in shouldAllowKeyVaultEmptyAccessPoliciesAllVersionsBicep
resource keyVaultPreview 'Microsoft.KeyVault/vaults@2021-06-01-preview' = {
  name: 'compliantKeyVaultPreview'
  location: location
  properties: {
    tenantId: subscription().tenantId
    sku: {
      family: 'A'
      name: 'standard'
    }
    enableRbacAuthorization: true
  }
}

// KeyVault with other empty properties should still raise issues
resource keyVaultWithEmptyNetworkAcls 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: 'noncompliantKeyVault'
  location: location
  properties: {
    tenantId: subscription().tenantId
    sku: {
      family: 'A'
      name: 'standard'
    }
    accessPolicies: []
    networkAcls: {} // Noncompliant {{Remove this empty object or complete with real code.}}
//  ^^^^^^^^^^^^^^^
  }
}

// Other resource types do not benefit from the KeyVault accessPolicies exemption
resource storageAccount 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: 'compliantStorage'
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    minimumTlsVersion: 'TLS1_2'
    encryption: {
      requireInfrastructureEncryption: true
      services: {
        blob: {
          enabled: true
        }
      }
      keySource: 'Microsoft.Storage'
    }
  }
}
