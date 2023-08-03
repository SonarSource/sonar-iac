resource Sensitive_Web_not_ClientCertificate 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
  name: 'Sensitive: \'Web\' + not \'ClientCertificate\''
  properties: {
    type: 'Web'
    typeProperties: {
      authenticationType: 'Basic' // Noncompliant{{This authentication method is not certificate-based. Make sure it is safe here.}}
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource Sensitive_HttpServer_not_ClientCertificate 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
  name: 'Sensitive: \'HttpServer\' + not \'ClientCertificate\''
  properties: {
    type: 'HttpServer'
    typeProperties: {
      authenticationType: 'Basic' // Noncompliant
    }
  }
}

resource Sensitive_Web_not_ClientCertificate_in_nested_resource 'Microsoft.DataFactory/factories@2018-06-01' = {
  name: 'Sensitive: \'Web\' + not \'ClientCertificate\' in nested resource'

  resource Sensitive_Web_not_ClientCertificate_in_nested_resource_Sensitive_nested_resource 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
    name: 'Sensitive nested resource'
    properties: {
      type: 'Web'
      typeProperties: {
        authenticationType: 'Basic' // Noncompliant
      }
    }
  }
}

resource Compliant_Web_ClientCertificate 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
  name: 'Compliant: \'Web\' + \'ClientCertificate\''
  properties: {
    type: 'Web'
    typeProperties: {
      authenticationType: 'ClientCertificate'
    }
  }
}

resource Compliant_HttpServer_ClientCertificate 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
  name: 'Compliant: \'HttpServer\' + \'ClientCertificate\''
  properties: {
    type: 'HttpServer'
    typeProperties: {
      authenticationType: 'ClientCertificate'
    }
  }
}

resource Compliant_other_than_Web_HttpServer_not_ClientCertificate 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
  name: 'Compliant: other than \'Web\'/\'HttpServer\' + not \'ClientCertificate\''
  properties: {
    type: 'Other'
    typeProperties: {
      authenticationType: 'Basic'
    }
  }
}

resource Compliant 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
  name: 'Compliant'
  properties: {
    type: 'Web'
    typeProperties: {
      authenticationType: 'ClientCertificate'
    }
  }
}
