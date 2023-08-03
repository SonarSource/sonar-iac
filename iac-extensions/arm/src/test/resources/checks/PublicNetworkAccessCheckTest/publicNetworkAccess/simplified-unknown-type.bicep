resource Compliant_Should_NOT_raise_issue_for_Enabled_unknown_type 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for Enabled, unknown type'
  properties: {
    publicNetworkAccess: 'Enabled'
  }
}

resource Compliant_Should_NOT_raise_issue_for_Disabled 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for Disabled'
  properties: {
    publicNetworkAccess: 'Disabled'
  }
}

resource Compliant_Should_NOT_raise_issue_for_unknown_value 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for unknown value'
  properties: {
    publicNetworkAccess: 'unknown'
  }
}

resource Compliant_Should_NOT_raise_issue_for_missing_publicNetworkAccess 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for missing publicNetworkAccess'
  properties: {}
}

resource Compliant_Should_NOT_raise_issue_for_publicNetworkAccess_as_not_StringLiteral 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for publicNetworkAccess as not StringLiteral'
  properties: {
    publicNetworkAccess: []
  }
}