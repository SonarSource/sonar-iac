resource noncompliant1 'Microsoft.SignalRService/signalR@2021-10-01' = {
  name: 'Sensitive: clientCertEnabled is not set'
  properties: {
    publicNetworkAccess: 'Disabled'
    tls: {} // Noncompliant{{Set "clientCertEnabled" to enable client certificate authentication.}}
//       ^^
  }
}

resource compliant_existing 'Microsoft.SignalRService/signalR@2021-10-01' existing = {
  name: 'Compliant: existing'
}

resource noncompliant2 'Microsoft.SignalRService/signalR@2021-10-01' = {
  name: 'Sensitive: clientCertEnabled is set to false'
  properties: {
    publicNetworkAccess: 'Disabled'
    tls: {
      clientCertEnabled: false // Noncompliant{{Enable client certificate authentication for this resource.}}
//    ^^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource compliant1 'Microsoft.SignalRService/signalR@2021-10-01' = {
  name: 'Compliant: clientCertEnabled is set to true'
  properties: {
    publicNetworkAccess: 'Disabled'
    tls: {
      clientCertEnabled: true
    }
  }
}

resource compliant2 'another type@2021-10-01' = {
  name: 'Compliant: the resource type is not concerned by this rule'
  properties: {
    tls: {
      clientCertEnabled: false
    }
  }
}

resource compliant3 'Microsoft.SignalRService/signalR@2021-10-01' = {
  name: 'Compliant: publicNetworkAccess not Disabled'
  properties: {
    tls: {
      clientCertEnabled: false
    }
  }
}
