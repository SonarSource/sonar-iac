resource Compliant_Should_NOT_raise_issue_for_Enabled 'unkown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for Enabled'
  properties: {
    siteConfig: {
      publicNetworkAccess: 'Enabled'
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_Disabled 'unkown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for Disabled'
  properties: {
    siteConfig: {
      publicNetworkAccess: 'Disabled'
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_unknown_value 'unkown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for unknown value'
  properties: {
    siteConfig: {
      publicNetworkAccess: 'unknown'
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_missing_publicNetworkAccess 'unkown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for missing publicNetworkAccess'
  properties: {
    siteConfig: {}
  }
}

resource Compliant_Should_NOT_raise_issue_for_publicNetworkAccess_as_not_StringLiteral 'unkown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for publicNetworkAccess as not StringLiteral'
  properties: {
    siteConfig: {
      publicNetworkAccess: []
    }
  }
}