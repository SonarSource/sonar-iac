// Noncompliant@+1{{Omitting "trustedRootCertificates" disables certificate-based authentication. Make sure it is safe here.}}
resource Sensitive_trustedRootCertificates_property_is_missing 'Microsoft.Network/applicationGateways@2020-06-01' = {
  name: 'Sensitive: trustedRootCertificates property is missing'
  properties: {}
}

resource Sensitive_trustedRootCertificates_array_is_empty 'Microsoft.Network/applicationGateways@2020-06-01' = {
  name: 'Sensitive: trustedRootCertificates array is empty'
  properties: {
    trustedRootCertificates: [] // Noncompliant{{Omitting a list of certificates disables certificate-based authentication. Make sure it is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource Compliant_trustedRootCertificates_array_is_defined_and_not_empty 'Microsoft.Network/applicationGateways@2020-06-01' = {
  name: 'Compliant: trustedRootCertificates array is defined and not empty'
  properties: {
    trustedRootCertificates: [
      {
        '...': 'certificate details'
      }
    ]
  }
}
