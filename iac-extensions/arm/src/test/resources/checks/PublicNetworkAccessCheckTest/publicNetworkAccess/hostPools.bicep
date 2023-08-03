resource Sensitive_Should_raise_issue_for_Enabled 'Microsoft.DesktopVirtualization/hostPools@2021-04-01-preview' = {
  name: 'Sensitive: Should raise issue for Enabled'
  properties: {
    publicNetworkAccess: 'Enabled' // Noncompliant{{Make sure allowing public network access is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource Sensitive_Should_raise_issue_for_EnabledForSessionHostsOnly 'Microsoft.DesktopVirtualization/hostPools@2021-04-01-preview' = {
  name: 'Sensitive: Should raise issue for EnabledForSessionHostsOnly'
  properties: {
    publicNetworkAccess: 'EnabledForSessionHostsOnly' // Noncompliant
  }
}

resource Sensitive_Should_raise_issue_for_EnabledForClientsOnly 'Microsoft.DesktopVirtualization/hostPools@2021-04-01-preview' = {
  name: 'Sensitive: Should raise issue for EnabledForClientsOnly'
  properties: {
    publicNetworkAccess: 'EnabledForClientsOnly' // Noncompliant
  }
}

resource Compliant_Should_NOT_raise_issue_for_Disabled 'Microsoft.DesktopVirtualization/hostPools@2021-04-01-preview' = {
  name: 'Compliant: Should NOT raise issue for Disabled'
  properties: {
    publicNetworkAccess: 'Disabled'
  }
}

resource Compliant_Should_NOT_raise_issue_for_unknown_value 'Microsoft.DesktopVirtualization/hostPools@2021-04-01-preview' = {
  name: 'Compliant: Should NOT raise issue for unknown value'
  properties: {
    publicNetworkAccess: 'unknown'
  }
}

resource Compliant_Should_NOT_raise_issue_for_missing_publicNetworkAccess 'Microsoft.DesktopVirtualization/hostPools@2021-04-01-preview' = {
  name: 'Compliant: Should NOT raise issue for missing publicNetworkAccess'
  properties: {}
}

resource Compliant_Should_NOT_raise_issue_for_publicNetworkAccess_as_not_StringLiteral 'Microsoft.DesktopVirtualization/hostPools@2021-04-01-preview' = {
  name: 'Compliant: Should NOT raise issue for publicNetworkAccess as not StringLiteral'
  properties: {
    publicNetworkAccess: []
  }
}
