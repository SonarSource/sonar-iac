resource Microsoft_ApiManagement_service_apis_Raise_issue_because_protocols_contains_http 'Microsoft.ApiManagement/service/apis@2022-08-01' = {
  name: 'Raise issue because protocols contains http'
  properties: {
    protocols: [
      'http' // Noncompliant{{Make sure that using clear-text protocols is safe here.}}
//    ^^^^^^
    ]
  }
}

resource Microsoft_ApiManagement_service_apis_Raise_issue_because_protocols_contains_http 'Microsoft.ApiManagement/service/apis@2022-08-01' = {
  name: 'Raise issue because protocols contains http'
  properties: {
    protocols: [
      'value'
      'http' // Noncompliant
      'other value'
    ]
  }
}

resource Compliant1 'Microsoft.ApiManagement/service/apis@2022-08-01' = {
  name: 'Compliant1'
  properties: {
    protocols: [
      'https'
    ]
  }
}

resource Compliant2 'Microsoft.ApiManagement/service/apis@2022-08-01' = {
  name: 'Compliant2'
  properties: {}
}

resource Compliant3 'Microsoft.ApiManagement/service/apis@2022-08-01' = {
  name: 'Compliant3'
  properties: {
    protocols: [
      'value'
      'other value'
    ]
  }
}
