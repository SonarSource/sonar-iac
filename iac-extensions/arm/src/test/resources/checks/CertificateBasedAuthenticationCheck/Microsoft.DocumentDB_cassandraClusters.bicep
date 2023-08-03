// Noncompliant@+1{{Omitting "clientCertificates" disables certificate-based authentication. Make sure it is safe here.}}
resource Sensitive_property_clientCertificates_is_missing 'Microsoft.DocumentDB/cassandraClusters@2021-10-15' = {
  name: 'Sensitive: property \'clientCertificates\' is missing'
  properties: {}
}

resource Sensitive_property_clientCertificates_array_is_empty 'Microsoft.DocumentDB/cassandraClusters@2021-10-15' = {
  name: 'Sensitive: property \'clientCertificates\' array is empty'
  properties: {
    clientCertificates: [] // Noncompliant{{Omitting a list of certificates disables certificate-based authentication. Make sure it is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource Compliant_property_clientCertificates_array_is_defined_and_not_empty 'Microsoft.DocumentDB/cassandraClusters@2021-10-15' = {
  name: 'Compliant: property \'clientCertificates\' array is defined and not empty'
  properties: {
    clientCertificates: [
      {
        '...': 'certificate details'
      }
    ]
  }
}

resource Compliant_resource_type_is_not_concerned_by_this_rule 'another type@2021-10-15' = {
  name: 'Compliant: resource type is not concerned by this rule'
  properties: {
    clientCertificates: []
  }
}
