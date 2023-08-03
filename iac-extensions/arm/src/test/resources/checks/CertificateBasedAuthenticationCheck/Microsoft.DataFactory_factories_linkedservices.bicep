resource noncompliant1 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
  name: 'Sensitive: \'Web\' + not \'ClientCertificate\''
  properties: {
    type: 'Web'
    typeProperties: {
      authenticationType: 'Basic' // Noncompliant{{This authentication method is not certificate-based. Make sure it is safe here.}}
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource noncompliant2 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
  name: 'Sensitive: \'HttpServer\' + not \'ClientCertificate\''
  properties: {
    type: 'HttpServer'
    typeProperties: {
      authenticationType: 'Basic' // Noncompliant
    }
  }
}

resource noncompliant3 'Microsoft.DataFactory/factories@2018-06-01' = {
  name: 'Sensitive: \'Web\' + not \'ClientCertificate\' in nested resource'

  resource noncompliant3_nested_child 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
    name: 'Sensitive nested resource'
    properties: {
      type: 'Web'
      typeProperties: {
        authenticationType: 'Basic' // Noncompliant
      }
    }
  }
}

resource compliant1 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
  name: 'Compliant: \'Web\' + \'ClientCertificate\''
  properties: {
    type: 'Web'
    typeProperties: {
      authenticationType: 'ClientCertificate'
    }
  }
}

resource compliant2 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
  name: 'Compliant: \'HttpServer\' + \'ClientCertificate\''
  properties: {
    type: 'HttpServer'
    typeProperties: {
      authenticationType: 'ClientCertificate'
    }
  }
}

resource compliant3 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
  name: 'Compliant: other than \'Web\'/\'HttpServer\' + not \'ClientCertificate\''
  properties: {
    type: 'Other'
    typeProperties: {
      authenticationType: 'Basic'
    }
  }
}

resource compliant4 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
  name: 'Compliant'
  properties: {
    type: 'Web'
    typeProperties: {
      authenticationType: 'ClientCertificate'
    }
  }
}
