resource Sensitive_clientCertEnabled_is_not_set 'Microsoft.SignalRService/webPubSub@2021-10-01' = {
  name: 'Sensitive: clientCertEnabled is not set'
  properties: {
    tls: {} // Noncompliant{{Omitting "clientCertEnabled" disables certificate-based authentication. Make sure it is safe here.}}
//       ^^
  }
}

resource Sensitive_clientCertEnabled_is_set_to_false 'Microsoft.SignalRService/webPubSub@2021-10-01' = {
  name: 'Sensitive: clientCertEnabled is set to false'
  properties: {
    tls: {
      clientCertEnabled: false // Noncompliant{{Make sure that disabling certificate-based authentication is safe here.}}
//    ^^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource Compliant_clientCertEnabled_is_set_to_true 'Microsoft.SignalRService/webPubSub@2021-10-01' = {
  name: 'Compliant: clientCertEnabled is set to true'
  properties: {
    tls: {
      clientCertEnabled: true
    }
  }
}
