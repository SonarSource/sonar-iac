// Noncompliant@+1{{Set "negotiateClientCertificate" to enable client certificate authentication.}}
resource raise_an_issue_the_certificate_boolean_is_missing 'Microsoft.ApiManagement/service/gateways/hostnameConfigurations@2022-11-01' = {
  name: 'Raise an issue: the certificate boolean is missing'
  properties: {
    otherProperty: 'test'
  }
}

// Noncompliant@+1{{Set "negotiateClientCertificate" to enable client certificate authentication.}}
resource raise_an_issue_the_properties_is_missing 'Microsoft.ApiManagement/service/gateways/hostnameConfigurations@2022-11-01' = {
  name: 'Raise an issue: properties are not present'
  properties: {
    otherProperty: 'test'
  }
}

resource compliant_existing 'Microsoft.ApiManagement/service/gateways/hostnameConfigurations@2022-11-01' existing = {
  name: 'Compliant existing'
}

resource raise_an_issue_the_certificate_boolean_is_set_to_false 'Microsoft.ApiManagement/service/gateways/hostnameConfigurations@2022-11-01' = {
  name: 'Raise an issue: the certificate boolean is set to false'
  properties: {
    negotiateClientCertificate: false // Noncompliant{{Enable client certificate authentication for this resource.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

// Noncompliant@+1
resource raise_an_issue_the_property_case_is_incorrect 'Microsoft.ApiManagement/service/gateways/hostnameConfigurations@2022-11-01' = {
  name: 'Raise an issue: the property case is incorrect'
  properties: {
    Negotiateclientcertificate: true
  }
}

resource parent_resource 'Microsoft.ApiManagement/service/gateways@2022-11-01' = {
  name: 'Raise an issue: test with inner child'

  resource child_resource_noncompliant 'hostnameConfigurations@2022-11-01' = {
    name: 'Raise an issue: test with inner child'
    properties: {
      negotiateClientCertificate: false // Noncompliant
    }
  }
}

resource compliant 'Microsoft.ApiManagement/service/gateways/hostnameConfigurations@2022-11-01' = {
  name: 'Compliant: negotiateClientCertificate is set to true'
  properties: {
    negotiateClientCertificate: true
  }
}

resource do_not_raise_an_issue_the_property_value_is_not_a_boolean_as_expected_conservative_approach 'Microsoft.ApiManagement/service/gateways/hostnameConfigurations@2022-11-01' = {
  name: 'Compliant: the property value is not a boolean as expected, conservative approach'
  properties: {
    negotiateClientCertificate: 'false'
  }
}

resource Compliant_inner_child 'Microsoft.ApiManagement/service/gateways@2022-11-01' = {
  name: 'Compliant: inner child does have the property negotiateClientCertificate set to true'

  resource compliant_inner_child_raise_an_issue_test_with_inner_child 'hostnameConfigurations@2022-11-01' = {
    name: 'Raise an issue: test with inner child'
    properties: {
      negotiateClientCertificate: true
    }
  }
}
