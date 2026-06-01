// Noncompliant@+2{{Set "clientCertEnabled" to enable client certificate authentication.}}
// Noncompliant@+1{{Set "clientCertMode" to enable client certificate authentication.}}
resource noncompliant1 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: both clientCertEnabled and clientCertMode are missing'
  properties: {
    publicNetworkAccess: 'Disabled'
  }
}

resource compliant_existing 'Microsoft.Web/sites@2015-08-01' existing = {
  name: 'Compliant: existing'
}

// Noncompliant@+1{{Set "clientCertMode" to enable client certificate authentication.}}
resource noncompliant2 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is false and clientCertMode is missing'
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: false // Noncompliant{{Enable client certificate authentication for this resource.}}
  }
}

// Noncompliant@+1{{Set "clientCertMode" to enable client certificate authentication.}}
resource noncompliant3 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is true but clientCertMode is missing'
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: true
  }
}

resource noncompliant4 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is true but clientCertMode is not \'Required\''
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: true
    clientCertMode: 'Optional' // Noncompliant{{Require client certificates for this resource.}}
  }
}

resource noncompliant5 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is false and clientCertMode is \'Required\''
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: false // Noncompliant
    clientCertMode: 'Required'
  }
}

// Noncompliant@+1{{Set "clientCertEnabled" to enable client certificate authentication.}}
resource noncompliant6 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is missing (clientCertMode is \'Required\')'
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertMode: 'Required'
  }
}

resource noncompliant7 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is true but clientCertMode is \'OptionalInteractiveUser\''
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: true
    clientCertMode: 'OptionalInteractiveUser' // Noncompliant{{Require client certificates for this resource.}}
  }
}

resource compliant1 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Compliant: clientCertEnabled is true and clientCertMode is \'Required\''
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: true
    clientCertMode: 'Required'
  }
}

resource compliant2 'another type@2015-08-01' = {
  name: 'Compliant: the resource type is not in the scope of the rule'
  properties: {}
}

resource compliant3 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Compliant: publicNetworkAccess not Disabled'
  properties: {
    clientCertEnabled: false
  }
}
