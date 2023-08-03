resource Sensitive_Should_raise_issue_for_Enabled 'Microsoft.Insights/dataCollectionEndpoints@2021-04-01' = {
  name: 'Sensitive: Should raise issue for Enabled'
  properties: {
    networkAcls: {
      publicNetworkAccess: 'Enabled' // Noncompliant{{Make sure allowing public network access is safe here.}}
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_Disabled 'Microsoft.Insights/dataCollectionEndpoints@2021-04-01' = {
  name: 'Compliant: Should NOT raise issue for Disabled'
  properties: {
    networkAcls: {
      publicNetworkAccess: 'Disabled'
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_unknown 'Microsoft.Insights/dataCollectionEndpoints@2021-04-01' = {
  name: 'Compliant: Should NOT raise issue for unknown'
  properties: {
    networkAcls: {
      publicNetworkAccess: 'unknown'
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_missing_property 'Microsoft.Insights/dataCollectionEndpoints@2021-04-01' = {
  name: 'Compliant: Should NOT raise issue for missing property'
  properties: {
    networkAcls: {}
  }
}

resource Compliant_Should_NOT_raise_issue_for_publicNetworkAccess_as_not_StringLiteral 'Microsoft.Insights/dataCollectionEndpoints@2021-04-01' = {
  name: 'Compliant: Should NOT raise issue for publicNetworkAccess as not StringLiteral'
  properties: {
    networkAcls: {
      publicNetworkAccess: {}
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_unknown_type 'unknown-type@2021-04-01' = {
  name: 'Compliant: Should NOT raise issue for unknown type'
  properties: {
    networkAcls: {
      publicNetworkAccess: 'Enabled'
    }
  }
}
