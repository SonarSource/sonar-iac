resource Raise_issue_as_isHttpAllowed_is_set_to_true 'Microsoft.Cdn/profiles/endpoints@2022-07-01' = {
  name: 'Raise issue as isHttpAllowed is set to true'
  properties: {
    isHttpAllowed: true // Noncompliant{{Make sure that using clear-text protocols is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^
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
    isHttpAllowed: false
  }
}

resource Microsoft_Cdn_profiles_endpoints_Compliant_2 'Microsoft.Cdn/profiles/endpoints@2022-07-01' = {
  name: 'Compliant_2'
  properties: {
    isHttpAllowed: 'wrong format'
  }
}
