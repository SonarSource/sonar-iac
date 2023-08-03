resource Raise_issue_as_sslEnforcement_is_set_to_Disabled '${type}@2022-09-01' = {
  name: 'Raise issue as sslEnforcement is set to Disabled'
  properties: {
    sslEnforcement: 'Disabled' // Noncompliant{{Make sure that using clear-text protocols is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource Compliant '${type}@2022-09-01' = {
  name: 'Compliant'
  properties: {
    sslEnforcement: 'Enabled'
  }
}

resource Compliant_with_wrong_format '${type}@2022-09-01' = {
  name: 'Compliant with wrong format'
  properties: {
    sslEnforcement: true
  }
}
