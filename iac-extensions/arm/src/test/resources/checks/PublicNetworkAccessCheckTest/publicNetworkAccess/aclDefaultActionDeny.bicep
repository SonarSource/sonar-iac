// --- Compliant: an ACL defaultAction 'Deny' is present ---

// properties.networkAcls.defaultAction: 'Deny' (Cognitive Services / Storage / Key Vault / Service Bus / ...)
resource cognitiveWithAclDeny 'Microsoft.CognitiveServices/accounts@2023-05-01' = {
  name: 'translator-with-acl-deny'
  properties: {
    publicNetworkAccess: 'Enabled'
    networkAcls: {
      defaultAction: 'Deny'
      ipRules: [
        { value: '203.0.113.0/24' }
      ]
    }
  }
}

// properties.networkACLs.defaultAction: 'Deny' (SignalR / WebPubSub casing)
resource signalrWithAclDeny 'Microsoft.SignalRService/signalR@2023-02-01' = {
  name: 'signalr-with-acl-deny'
  properties: {
    publicNetworkAccess: 'Enabled'
    networkACLs: {
      defaultAction: 'Deny'
      publicNetwork: {
        allow: [ 'ClientConnection' ]
      }
    }
  }
}

// properties.siteConfig.ipSecurityRestrictionsDefaultAction: 'Deny' (App Service)
resource siteWithIpRestrictionsDeny 'Microsoft.Web/sites@2022-09-01' = {
  name: 'web-app-with-ip-restrictions'
  properties: {
    publicNetworkAccess: 'Enabled'
    siteConfig: {
      ipSecurityRestrictionsDefaultAction: 'Deny'
      ipSecurityRestrictions: [
        { ipAddress: '203.0.113.0/24', action: 'Allow' }
      ]
    }
  }
}

// publicNetworkAccess nested in siteConfig is suppressed by the sibling ipSecurityRestrictionsDefaultAction 'Deny'.
resource siteConfigNestedWithIpRestrictionsDeny 'Microsoft.Web/sites@2022-09-01' = {
  name: 'web-app-siteconfig-nested'
  properties: {
    siteConfig: {
      publicNetworkAccess: 'Enabled'
      ipSecurityRestrictionsDefaultAction: 'Deny'
    }
  }
}

// Broader sensitive value is covered too.
resource hostPoolWithAclDeny 'Microsoft.DesktopVirtualization/hostPools@2023-09-05' = {
  name: 'hostpool-with-acl-deny'
  properties: {
    publicNetworkAccess: 'EnabledForSessionHostsOnly'
    networkAcls: {
      defaultAction: 'Deny'
    }
  }
}

// --- Noncompliant: no ACL block, or ACL present without defaultAction 'Deny' ---

resource signalrNoAcl 'Microsoft.SignalRService/signalR@2023-02-01' = {
  name: 'no-acl'
  properties: {
    publicNetworkAccess: 'Enabled' // Noncompliant{{Make sure allowing public network access is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource cognitiveAclAllow 'Microsoft.CognitiveServices/accounts@2023-05-01' = {
  name: 'acl-allow'
  properties: {
    publicNetworkAccess: 'Enabled' // Noncompliant
    networkAcls: {
      defaultAction: 'Allow'
    }
  }
}

resource storageAclNoDefaultAction 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: 'acl-without-default-action'
  properties: {
    publicNetworkAccess: 'Enabled' // Noncompliant
    networkAcls: {
      ipRules: []
    }
  }
}

resource siteNoIpDefault 'Microsoft.Web/sites@2022-09-01' = {
  name: 'site-no-ip-default'
  properties: {
    publicNetworkAccess: 'Enabled' // Noncompliant
    siteConfig: {
      ipSecurityRestrictions: []
    }
  }
}
