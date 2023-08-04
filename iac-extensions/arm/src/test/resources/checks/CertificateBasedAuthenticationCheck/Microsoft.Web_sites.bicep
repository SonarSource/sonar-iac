// Noncompliant@+2{{Omitting "clientCertEnabled" disables certificate-based authentication. Make sure it is safe here.}}
// Noncompliant@+1{{Omitting "clientCertMode" disables certificate-based authentication. Make sure it is safe here.}}
resource noncompliant1 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: both clientCertEnabled and clientCertMode are missing'
  properties: {}
}

// Noncompliant@+1{{Omitting "clientCertMode" disables certificate-based authentication. Make sure it is safe here.}}
resource noncompliant2 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is false and clientCertMode is missing'
  properties: {
    clientCertEnabled: false // Noncompliant{{Make sure that disabling certificate-based authentication is safe here.}}
  }
}

// Noncompliant@+1{{Omitting "clientCertMode" disables certificate-based authentication. Make sure it is safe here.}}
resource noncompliant3 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is true but clientCertMode is missing'
  properties: {
    clientCertEnabled: true
  }
}

resource noncompliant4 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is true but clientCertMode is not \'Required\''
  properties: {
    clientCertEnabled: true
    clientCertMode: 'Optional' // Noncompliant{{Connections without client certificates will be permitted. Make sure it is safe here.}}
  }
}

resource noncompliant5 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is false and clientCertMode is \'Required\''
  properties: {
    clientCertEnabled: false // Noncompliant
    clientCertMode: 'Required'
  }
}

// Noncompliant@+1{{Omitting "clientCertEnabled" disables certificate-based authentication. Make sure it is safe here.}}
resource noncompliant6 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is missing and clientCertMode is not \'Required\''
  properties: {
    clientCertMode: 'Required'
  }
}

resource compliant1 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Compliant: clientCertEnabled is true and clientCertMode is \'Required\''
  properties: {
    clientCertEnabled: true
    clientCertMode: 'Required'
  }
}

resource compliant2 'another type@2015-08-01' = {
  name: 'Compliant: the resource type is not in the scope of the rule'
  properties: {}
}
