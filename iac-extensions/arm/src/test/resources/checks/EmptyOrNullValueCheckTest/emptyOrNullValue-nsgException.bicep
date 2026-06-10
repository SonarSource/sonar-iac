// Test for Network Security Groups (NSG) securityRules empty array should not raise issue.
// An explicit empty array means "no security rules" and Azure behaves differently
// when the property is absent vs present as an empty array.
// The exemption is version-agnostic: all API versions of Microsoft.Network/networkSecurityGroups are covered.

// Compliant - NSG with empty securityRules (version from the ticket)
resource nsg 'Microsoft.Network/networkSecurityGroups@2024-01-01' = {
  name: 'compliantNsg'
  location: 'eastus'
  properties: {
    securityRules: []
  }
}

// Compliant - NSG with older API version (SONARIAC-2689: version-agnostic exemption)
resource nsg2 'Microsoft.Network/networkSecurityGroups@2022-11-01' = {
  name: 'compliantNsgOtherVersion'
  location: 'eastus'
  properties: {
    securityRules: []
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
