resource noncompliant1 'Microsoft.SignalRService/signalR@2021-10-01' = {
  name: 'Sensitive: clientCertEnabled is not set'
  properties: {
    tls: {} // Noncompliant{{Omitting "clientCertEnabled" disables certificate-based authentication. Make sure it is safe here.}}
//       ^^
  }
}

resource noncompliant2 'Microsoft.SignalRService/signalR@2021-10-01' = {
  name: 'Sensitive: clientCertEnabled is set to false'
  properties: {
    tls: {
      clientCertEnabled: false // Noncompliant{{Make sure that disabling certificate-based authentication is safe here.}}
//    ^^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource compliant1 'Microsoft.SignalRService/signalR@2021-10-01' = {
  name: 'Compliant: clientCertEnabled is set to true'
  properties: {
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
