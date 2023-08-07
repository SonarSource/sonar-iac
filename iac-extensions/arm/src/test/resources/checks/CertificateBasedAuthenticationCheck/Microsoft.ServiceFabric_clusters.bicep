// Noncompliant@+1{{Omitting "clientCertificateCommonNames/clientCertificateThumbprints" disables certificate-based authentication. Make sure it is safe here.}}
resource noncompliant1 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
  name: 'Sensitive: both properties are not defined'
  properties: {}
}

// Noncompliant@+1{{Omitting a list of certificates disables certificate-based authentication. Make sure it is safe here.}}
resource noncompliant2 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
//                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'Sensitive: both properties are defined but empty'
  properties: {
    clientCertificateCommonNames: []
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{Empty certificate list}}
    clientCertificateThumbprints: []
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{Empty certificate list}}
  }
}

// Noncompliant@+1
resource noncompliant3 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
  name: 'Sensitive: only one property is defined but empty'
  properties: {
    clientCertificateThumbprints: []
  }
}

// Noncompliant@+1
resource noncompliant4 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
  name: 'Sensitive: only one property is defined but empty (bis)'
  properties: {
    clientCertificateCommonNames: []
  }
}

resource compliant1 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
  name: 'Compliant: one properties defined and not empty, the other is not defined'
  properties: {
    clientCertificateCommonNames: [
      {
        '...': 'certificate details'
      }
    ]
  }
}

resource compliant2 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
  name: 'Compliant: one properties defined and not empty, the other is not defined (bis)'
  properties: {
    clientCertificateThumbprints: [
      {
        '...': 'certificate details'
      }
    ]
  }
}

resource compliant3 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
  name: 'Compliant: one properties defined and not empty, the other is defined and empty'
  properties: {
    clientCertificateCommonNames: []
    clientCertificateThumbprints: [
      {
        '...': 'certificate details'
      }
    ]
  }
}

resource compliant4 'another type@2016-03-01' = {
  name: 'Compliant: resource type is not impacted by the check'
  properties: {
    clientCertificateThumbprints: []
  }
}
