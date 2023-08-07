resource nonCompliant1 '${type}@2017-12-01' = {
  name: 'Raise an issue: older TLS versions shouldn\'t be allowed'
  properties: {
    minimalTlsVersion: 'TLS1_0'
  }
}

resource nonCompliant2 '${type}@2017-12-01' = {
  name: 'Raise an issue: TLS version is absent'
  properties: {}
}

resource Compliant '${type}@2017-12-01' = {
  name: 'Compliant'
  properties: {
    minimalTlsVersion: 'TLS1_2'
  }
}
