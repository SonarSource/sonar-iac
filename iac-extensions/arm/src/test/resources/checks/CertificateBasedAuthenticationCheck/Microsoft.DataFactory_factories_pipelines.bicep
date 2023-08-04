resource noncompliant1 'Microsoft.DataFactory/factories/pipelines@2018-06-01' = {
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

resource noncompliant2 'Microsoft.DataFactory/factories@2018-06-01' = {
  name: 'Sensitive case of nested resource'

  resource noncompliant2_nested_child 'Microsoft.DataFactory/factories/pipelines@2018-06-01' = {
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

resource noncompliant3 'Microsoft.DataFactory/factories/pipelines@2018-06-01' = {
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

resource compliant1 'Microsoft.DataFactory/factories/pipelines@2018-06-01' = {
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

resource compliant2 'Microsoft.DataFactory/factories/pipelines@2018-06-01' = {
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

resource compliant3 'Microsoft.DataFactory/factories/pipelines@2018-06-01' = {
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
