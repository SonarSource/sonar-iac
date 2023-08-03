resource Raise_issue_as_httpsOnly_is_set_to_false 'Microsoft.Web/sites@2022-07-01' = {
  name: 'Raise issue as httpsOnly is set to false'
  properties: {
    httpsOnly: false // Noncompliant{{Make sure that using clear-text protocols is safe here.}}
//  ^^^^^^^^^^^^^^^^
  }
}

// Noncompliant@+1{{Omitting "httpsOnly" allows the use of clear-text protocols. Make sure it is safe here.}}
resource Raise_issue_as_httpsOnly_is_missing 'Microsoft.Web/sites@2022-07-01' = {
  name: 'Raise issue as httpsOnly is missing'
  properties: {}
}

resource Microsoft_Web_sites_Compliant_1 'Microsoft.Web/sites@2022-07-01' = {
  name: 'Compliant_1'
  properties: {
    httpsOnly: true
  }
}

resource Microsoft_Web_sites_Compliant_2 'Microsoft.Web/sites@2022-07-01' = {
  name: 'Compliant_2'
  properties: {
    httpsOnly: 'wrong format'
  }
}
