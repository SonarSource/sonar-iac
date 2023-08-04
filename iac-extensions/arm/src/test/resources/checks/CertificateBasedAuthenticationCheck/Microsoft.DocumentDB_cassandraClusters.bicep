// Noncompliant@+1{{Omitting "clientCertificates" disables certificate-based authentication. Make sure it is safe here.}}
resource noncompliant1 'Microsoft.DocumentDB/cassandraClusters@2021-10-15' = {
  name: 'Sensitive: property \'clientCertificates\' is missing'
  properties: {}
}

resource noncompliant2 'Microsoft.DocumentDB/cassandraClusters@2021-10-15' = {
  name: 'Sensitive: property \'clientCertificates\' array is empty'
  properties: {
    clientCertificates: [] // Noncompliant{{Omitting a list of certificates disables certificate-based authentication. Make sure it is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource compliant1 'Microsoft.DocumentDB/cassandraClusters@2021-10-15' = {
  name: 'Compliant: property \'clientCertificates\' array is defined and not empty'
  properties: {
    clientCertificates: [
      {
        '...': 'certificate details'
      }
    ]
  }
}

resource compliant2 'another type@2021-10-15' = {
  name: 'Compliant: resource type is not concerned by this rule'
  properties: {
    clientCertificates: []
  }
}
