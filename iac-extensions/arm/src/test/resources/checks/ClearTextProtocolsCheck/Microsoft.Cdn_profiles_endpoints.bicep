resource Raise_issue_as_httpsOnly_is_set_to_false 'Microsoft.Cdn/profiles/endpoints@2022-07-01' = {
  name: 'Raise issue as httpsOnly is set to false'
  properties: {
    isHttpAllowed: false // Noncompliant{{Make sure that using clear-text protocols is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^
  }
}

// Noncompliant@+1{{Omitting "isHttpAllowed" allows the use of clear-text protocols. Make sure it is safe here.}}
resource Raise_issue_as_isHttpAllowed_is_missing 'Microsoft.Cdn/profiles/endpoints@2022-07-01' = {
  name: 'Raise issue as isHttpAllowed is missing'
  properties: {}
}

resource Microsoft_Cdn_profiles_endpoints_Compliant_1 'Microsoft.Cdn/profiles/endpoints@2022-07-01' = {
  name: 'Compliant_1'
  properties: {
    isHttpAllowed: true
  }
}

resource Microsoft_Cdn_profiles_endpoints_Compliant_2 'Microsoft.Cdn/profiles/endpoints@2022-07-01' = {
  name: 'Compliant_2'
  properties: {
    isHttpAllowed: 'wrong format'
  }
}
