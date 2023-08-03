resource Sensitive_Should_raise_issue_for_Enabled_1 'Microsoft.EventHub/namespaces@dummy' = {
  name: 'Sensitive: Should raise issue for Enabled 1'
  resource Sensitive_Should_raise_issue_for_Enabled_1_name 'networkRuleSets@dummy' = {
    name: 'Sensitive: Should raise issue for Enabled 1/name'
    properties: {
      publicNetworkAccess: 'Enabled' // Noncompliant{{Make sure allowing public network access is safe here.}}
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource Sensitive_Should_raise_issue_for_Enabled_2 'Microsoft.HealthcareApis/workspaces@dummy' = {
  name: 'Sensitive: Should raise issue for Enabled 2'
  resource Sensitive_Should_raise_issue_for_Enabled_2_name 'dicomservices@dummy' = {
    name: 'Sensitive: Should raise issue for Enabled 2/name'
    properties: {
      publicNetworkAccess: 'Enabled' // Noncompliant
    }
  }
}

resource Sensitive_Should_raise_issue_for_Enabled_3 'Microsoft.HealthcareApis/workspaces@dummy' = {
  name: 'Sensitive: Should raise issue for Enabled 3'
  resource Sensitive_Should_raise_issue_for_Enabled_3_name 'fhirservices@dummy' = {
    name: 'Sensitive: Should raise issue for Enabled 3/name'
    properties: {
      publicNetworkAccess: 'Enabled' // Noncompliant
    }
  }
}

resource Sensitive_Should_raise_issue_for_Enabled_4 'Microsoft.MachineLearningServices/workspaces@dummy' = {
  name: 'Sensitive: Should raise issue for Enabled 4'
  resource Sensitive_Should_raise_issue_for_Enabled_4_name 'onlineEndpoints@dummy' = {
    name: 'Sensitive: Should raise issue for Enabled 4/name'
    properties: {
      publicNetworkAccess: 'Enabled' // Noncompliant
    }
  }
}

resource Sensitive_Should_raise_issue_for_Enabled_5 'Microsoft.Relay/namespaces@dummy' = {
  name: 'Sensitive: Should raise issue for Enabled 5'
  resource Sensitive_Should_raise_issue_for_Enabled_5_name 'networkRuleSets@dummy' = {
    name: 'Sensitive: Should raise issue for Enabled 5/name'
    properties: {
      publicNetworkAccess: 'Enabled' // Noncompliant
    }
  }
}

resource Sensitive_Should_raise_issue_for_Enabled_6 'Microsoft.ServiceBus/namespaces@dummy' = {
  name: 'Sensitive: Should raise issue for Enabled 6'
  resource Sensitive_Should_raise_issue_for_Enabled_6_name 'networkRuleSets@dummy' = {
    name: 'Sensitive: Should raise issue for Enabled 6/name'
    properties: {
      publicNetworkAccess: 'Enabled' // Noncompliant
    }
  }
}

resource Sensitive_Should_raise_issue_for_Enabled_7 'Microsoft.Web/sites@dummy' = {
  name: 'Sensitive: Should raise issue for Enabled 7'
  resource Sensitive_Should_raise_issue_for_Enabled_7_name 'config@dummy' = {
    name: 'Sensitive: Should raise issue for Enabled 7/name'
    properties: {
      publicNetworkAccess: 'Enabled' // Noncompliant
    }
  }
}

resource Sensitive_Should_raise_issue_for_Enabled_8 'Microsoft.Web/sites@dummy' = {
  name: 'Sensitive: Should raise issue for Enabled 8'
  resource Sensitive_Should_raise_issue_for_Enabled_8_name 'slots@dummy' = {
    name: 'Sensitive: Should raise issue for Enabled 8/name'
    properties: {
      publicNetworkAccess: 'Enabled' // Noncompliant
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_Disabled 'Microsoft.Web/sites@dummy' = {
  name: 'Compliant: Should NOT raise issue for Disabled'
  resource Compliant_Should_NOT_raise_issue_for_Disabled_name 'slots@dummy' = {
    name: 'Compliant: Should NOT raise issue for Disabled/name'
    properties: {
      publicNetworkAccess: 'Disabled'
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_missing_publicNetworkAccess 'Microsoft.Web/sites@dummy' = {
  name: 'Compliant: Should NOT raise issue for missing publicNetworkAccess'
  resource Compliant_Should_NOT_raise_issue_for_missing_publicNetworkAccess_name 'slots@dummy' = {
    name: 'Compliant: Should NOT raise issue for missing publicNetworkAccess/name'
    properties: {}
  }
}

resource Compliant_Should_NOT_raise_issue_for_Enabled_for_unknown_type_and_subtype 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for Enabled for unknown type and subtype'
  resource Compliant_Should_NOT_raise_issue_for_Enabled_for_unknown_type_and_subtype_name 'unknown-sub-type@dummy' = {
    name: 'Compliant: Should NOT raise issue for Enabled for unknown type and subtype/name'
    properties: {
      publicNetworkAccess: 'Enabled'
    }
  }
}
