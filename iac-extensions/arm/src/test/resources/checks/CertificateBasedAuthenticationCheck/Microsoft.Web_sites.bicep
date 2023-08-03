// Noncompliant@+2{{Omitting "clientCertEnabled" disables certificate-based authentication. Make sure it is safe here.}}
// Noncompliant@+1{{Omitting "clientCertMode" disables certificate-based authentication. Make sure it is safe here.}}
resource Sensitive_both_clientCertEnabled_and_clientCertMode_are_missing 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: both clientCertEnabled and clientCertMode are missing'
  properties: {}
}

// Noncompliant@+1{{Omitting "clientCertMode" disables certificate-based authentication. Make sure it is safe here.}}
resource Sensitive_clientCertEnabled_is_false_and_clientCertMode_is_missing 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is false and clientCertMode is missing'
  properties: {
    clientCertEnabled: false // Noncompliant{{Make sure that disabling certificate-based authentication is safe here.}}
  }
}

// Noncompliant@+1{{Omitting "clientCertMode" disables certificate-based authentication. Make sure it is safe here.}}
resource Sensitive_clientCertEnabled_is_true_but_clientCertMode_is_missing 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is true but clientCertMode is missing'
  properties: {
    clientCertEnabled: true
  }
}

resource Sensitive_clientCertEnabled_is_true_but_clientCertMode_is_not_Required 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is true but clientCertMode is not \'Required\''
  properties: {
    clientCertEnabled: true
    clientCertMode: 'Optional' // Noncompliant{{Connections without client certificates will be permitted. Make sure it is safe here.}}
  }
}

resource Sensitive_clientCertEnabled_is_false_and_clientCertMode_is_Required 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is false and clientCertMode is \'Required\''
  properties: {
    clientCertEnabled: false // Noncompliant
    clientCertMode: 'Required'
  }
}

// Noncompliant@+1{{Omitting "clientCertEnabled" disables certificate-based authentication. Make sure it is safe here.}}
resource Sensitive_clientCertEnabled_is_missing_and_clientCertMode_is_not_Required 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is missing and clientCertMode is not \'Required\''
  properties: {
    clientCertMode: 'Required'
  }
}

resource Compliant_clientCertEnabled_is_true_and_clientCertMode_is_Required 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Compliant: clientCertEnabled is true and clientCertMode is \'Required\''
  properties: {
    clientCertEnabled: true
    clientCertMode: 'Required'
  }
}

resource Compliant_the_resource_type_is_not_in_the_scope_of_the_rule 'another type@2015-08-01' = {
  name: 'Compliant: the resource type is not in the scope of the rule'
  properties: {}
}
