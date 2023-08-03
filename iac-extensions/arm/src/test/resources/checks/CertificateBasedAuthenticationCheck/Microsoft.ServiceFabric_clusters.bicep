// Noncompliant@+1{{Omitting "clientCertificateCommonNames/clientCertificateThumbprints" disables certificate-based authentication. Make sure it is safe here.}}
resource Sensitive_both_properties_are_not_defined 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
  name: 'Sensitive: both properties are not defined'
  properties: {}
}

// Noncompliant@+1{{Omitting a list of certificates disables certificate-based authentication. Make sure it is safe here.}}
resource Sensitive_both_properties_are_defined_but_empty 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
//                                                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'Sensitive: both properties are defined but empty'
  properties: {
    clientCertificateCommonNames: []
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{Empty certificate list}}
    clientCertificateThumbprints: []
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^< {{Empty certificate list}}
  }
}

// Noncompliant@+1
resource Sensitive_only_one_property_is_defined_but_empty 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
  name: 'Sensitive: only one property is defined but empty'
  properties: {
    clientCertificateThumbprints: []
  }
}

// Noncompliant@+1
resource Sensitive_only_one_property_is_defined_but_empty_bis 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
  name: 'Sensitive: only one property is defined but empty (bis)'
  properties: {
    clientCertificateCommonNames: []
  }
}

resource Compliant_one_properties_defined_and_not_empty_the_other_is_not_defined 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
  name: 'Compliant: one properties defined and not empty, the other is not defined'
  properties: {
    clientCertificateCommonNames: [
      {
        '...': 'certificate details'
      }
    ]
  }
}

resource Compliant_one_properties_defined_and_not_empty_the_other_is_not_defined_bis 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
  name: 'Compliant: one properties defined and not empty, the other is not defined (bis)'
  properties: {
    clientCertificateThumbprints: [
      {
        '...': 'certificate details'
      }
    ]
  }
}

resource Compliant_one_properties_defined_and_not_empty_the_other_is_defined_and_empty 'Microsoft.ServiceFabric/clusters@2016-03-01' = {
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

resource Compliant_resource_type_is_not_impacted_by_the_check 'another type@2016-03-01' = {
  name: 'Compliant: resource type is not impacted by the check'
  properties: {
    clientCertificateThumbprints: []
  }
}
