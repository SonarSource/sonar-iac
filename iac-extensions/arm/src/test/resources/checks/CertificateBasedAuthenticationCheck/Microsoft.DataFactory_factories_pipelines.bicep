resource Sensitive_type_is_WebActivity_and_authenticationType_is_Basic 'Microsoft.DataFactory/factories/pipelines@2018-06-01' = {
  name: 'Sensitive: type is \'WebActivity\' and authenticationType is \'Basic\''
  properties: {
    activities: [
      {
        type: 'WebActivity'
//      ^^^^^^^^^^^^^^^^^^^> {{Pipeline type}}
        typeProperties: {
          authenticationType: 'Basic' // Noncompliant{{This authentication method is not certificate-based. Make sure it is safe here.}}
//        ^^^^^^^^^^^^^^^^^^^^^^^^^^^
        }
      }
    ]
  }
}

resource Sensitive_case_of_nested_resource 'Microsoft.DataFactory/factories@2018-06-01' = {
  name: 'Sensitive case of nested resource'

  resource Sensitive_case_of_nested_resource_Nested_resource 'Microsoft.DataFactory/factories/pipelines@2018-06-01' = {
    name: 'Nested resource'
    properties: {
      activities: [
        {
          type: 'WebActivity'
          typeProperties: {
            authenticationType: 'Basic' // Noncompliant
          }
        }
      ]
    }
  }
}

resource Sensitive_type_is_WebHook_and_authenticationType_is_ServicePrincipal 'Microsoft.DataFactory/factories/pipelines@2018-06-01' = {
  name: 'Sensitive: type is \'WebHook\' and authenticationType is \'ServicePrincipal\''
  properties: {
    activities: [
      {
        type: 'WebHook'
        typeProperties: {
          authenticationType: 'ServicePrincipal' // Noncompliant
        }
      }
    ]
  }
}

resource Compliant_type_is_WebActivity_but_authentication_type_is_ClientCertificate 'Microsoft.DataFactory/factories/pipelines@2018-06-01' = {
  name: 'Compliant: type is \'WebActivity\' but authentication type is \'ClientCertificate\''
  properties: {
    activities: [
      {
        type: 'WebActivity'
        typeProperties: {
          authenticationType: 'ClientCertificate'
        }
      }
    ]
  }
}

resource Compliant_type_is_Other_even_when_authentication_type_is_ServicePrincipal 'Microsoft.DataFactory/factories/pipelines@2018-06-01' = {
  name: 'Compliant: type is \'Other\' even when authentication type is \'ServicePrincipal\''
  properties: {
    activities: [
      {
        type: 'Other'
        typeProperties: {
          authenticationType: 'ServicePrincipal'
        }
      }
    ]
  }
}

resource Compliant_both_values_are_not_sensitive_type_is_Other_and_authentication_type_is_ClientCertificate 'Microsoft.DataFactory/factories/pipelines@2018-06-01' = {
  name: 'Compliant: both values are not sensitive, type is \'Other\' and authentication type is \'ClientCertificate\''
  properties: {
    activities: [
      {
        type: 'Other'
        typeProperties: {
          authenticationType: 'ClientCertificate'
        }
      }
    ]
  }
}
