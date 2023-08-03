// Noncompliant@+1{{Omitting "trustedRootCertificates" disables certificate-based authentication. Make sure it is safe here.}}
resource noncompliant1 'Microsoft.Network/applicationGateways@2020-06-01' = {
  name: 'Sensitive: trustedRootCertificates property is missing'
  properties: {}
}

resource noncompliant2 'Microsoft.Network/applicationGateways@2020-06-01' = {
  name: 'Sensitive: trustedRootCertificates array is empty'
  properties: {
    trustedRootCertificates: [] // Noncompliant{{Omitting a list of certificates disables certificate-based authentication. Make sure it is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource compliant1 'Microsoft.Network/applicationGateways@2020-06-01' = {
  name: 'Compliant: trustedRootCertificates array is defined and not empty'
  properties: {
    trustedRootCertificates: [
      {
        '...': 'certificate details'
      }
    ]
  }
}
