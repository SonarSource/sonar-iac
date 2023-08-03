resource Sensitive_Should_raise_issue_for_Enabled '${type}@dummy' = {
  name: 'Sensitive: Should raise issue for Enabled'
  properties: {
    publicNetworkAccess: 'Enabled' // Noncompliant{{Make sure allowing public network access is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource Compliant_Should_NOT_raise_issue_for_Disabled '${type}@dummy' = {
  name: 'Compliant: Should NOT raise issue for Disabled'
  properties: {
    publicNetworkAccess: 'Disabled'
  }
}

resource Compliant_Should_NOT_raise_issue_for_unknown_value '${type}@dummy' = {
  name: 'Compliant: Should NOT raise issue for unknown value'
  properties: {
    publicNetworkAccess: 'unknown'
  }
}

resource Compliant_Should_NOT_raise_issue_for_missing_publicNetworkAccess '${type}@dummy' = {
  name: 'Compliant: Should NOT raise issue for missing publicNetworkAccess'
  properties: {}
}

resource Compliant_Should_NOT_raise_issue_for_publicNetworkAccess_as_not_StringLiteral '${type}@dummy' = {
  name: 'Compliant: Should NOT raise issue for publicNetworkAccess as not StringLiteral'
  properties: {
    publicNetworkAccess: []
  }
}
