// Settings for MySQL
resource nonCompliant1 'Microsoft.DBforMySQL/servers@2017-12-01' = {
  name: 'Raise an issue: older TLS versions shouldn\'t be allowed'
  properties: {
    minimalTlsVersion: 'TLS1_0'
  }
}

// Settings for PostgreSQL
resource nonCompliant2 'Microsoft.DBforPostgreSQL/servers@2017-12-01' = {
  name: 'Raise an issue: TLS version is absent'
  properties: {}
}
