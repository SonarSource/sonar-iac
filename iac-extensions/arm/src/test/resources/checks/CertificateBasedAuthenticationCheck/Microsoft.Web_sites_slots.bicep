resource Sensitive_clientCertEnabled_is_false 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is false'
  properties: {
    clientCertEnabled: false // Noncompliant{{Make sure that disabling certificate-based authentication is safe here.}}
  }
}

resource Sensitive_clientCertMode_is_not_Required 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Sensitive: clientCertMode is not \'Required\''
  properties: {
    clientCertMode: 'Optional' // Noncompliant{{Connections without client certificates will be permitted. Make sure it is safe here.}}
  }
}

resource Sensitive_clientCertEnabled_is_true_but_clientCertMode_is_not_Required 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is true but clientCertMode is not \'Required\''
  properties: {
    clientCertEnabled: true
    clientCertMode: 'Optional' // Noncompliant
  }
}

resource Sensitive_clientCertEnabled_is_false_but_clientCertMode_is_Required 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is false but clientCertMode is \'Required\''
  properties: {
    clientCertEnabled: false // Noncompliant
    clientCertMode: 'Required'
  }
}

resource Sensitive_x2_parent_resource_is_compliant_but_nested_child_resource_override_with_unsafe_values 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive x2: parent resource is compliant but nested child resource override with unsafe values'
  properties: {
    clientCertEnabled: true
    clientCertMode: 'Required'
  }

  resource Sensitive_x2_parent_resource_is_compliant_but_nested_child_resource_override_with_unsafe_values_Nested_child_1 'slots@2015-08-01' = {
    name: 'Nested child 1'
    properties: {
      clientCertEnabled: false // Noncompliant
    }
  }

  resource Sensitive_x2_parent_resource_is_compliant_but_nested_child_resource_override_with_unsafe_values_Nested_child_2 'slots@2015-08-01' = {
    name: 'Nested child 2'
    properties: {
      clientCertMode: 'Optional' // Noncompliant
    }
  }
}

resource Sensitive_parent_is_sensitive_even_if_child_override_with_compliant_value_it_s_still_sensitive 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: parent is sensitive, even if child override with compliant value it\'s still sensitive'
  properties: {
    clientCertEnabled: false // Noncompliant
    clientCertMode: 'Required'
  }

  resource Sensitive_parent_is_sensitive_even_if_child_override_with_compliant_value_it_s_still_sensitive_Nested_child 'Microsoft.Web/sites/slots@2015-08-01' = {
    name: 'Nested child'
    properties: {
      clientCertEnabled: true
    }
  }
}

resource Compliant_clientCertEnabled_is_missing_and_clientCertMode_is_defined_and_is_Required 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Compliant: clientCertEnabled is missing and clientCertMode is defined and is \'Required\''
  properties: {
    clientCertMode: 'Required'
  }
}

resource Compliant_clientCertEnabled_is_true_and_clientCertMode_is_missing 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Compliant: clientCertEnabled is true and clientCertMode is missing'
  properties: {
    clientCertEnabled: true
  }
}

resource Compliant_clientCertEnabled_is_true_and_clientCertMode_is_Required 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Compliant: clientCertEnabled is true and clientCertMode is \'Required\''
  properties: {
    clientCertEnabled: true
    clientCertMode: 'Required'
  }
}

resource Compliant_parent_is_compliant_and_child_override_with_compliant_value 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Compliant: parent is compliant and child override with compliant value'
  properties: {
    clientCertEnabled: true
    clientCertMode: 'Required'
  }

  resource Compliant_parent_is_compliant_and_child_override_with_compliant_value_Nested_child 'Microsoft.Web/sites/slots@2015-08-01' = {
    name: 'Nested child'
    properties: {
      clientCertEnabled: true
    }
  }
}

resource Compliant_both_clientCertEnabled_and_clientCertMode_are_missing 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Compliant: both clientCertEnabled and clientCertMode are missing'
  properties: {}
}

resource Compliant_the_resource_type_is_not_in_the_scope_of_the_rule 'another type@2015-08-01' = {
  name: 'Compliant: the resource type is not in the scope of the rule'
  properties: {
    clientCertEnabled: false
  }
}
