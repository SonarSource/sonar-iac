resource noncompliant 'Microsoft.DocumentDB/databaseAccounts@2023-04-15' = {
  name: 'Sensitive: properties.backupPolicy.type is Periodic and backupRetentionIntervalInHours / 24 < 15 days'
  properties: {
    backupPolicy: {
      type: 'Periodic'
      periodicModeProperties: {
        backupRetentionIntervalInHours: 24 // Noncompliant
      }
    }
  }
}

resource compliant 'Microsoft.DocumentDB/databaseAccounts@2023-04-15' = {
  name: 'Compliant: properties.backupPolicy.type is Periodic and backupRetentionIntervalInHours / 24 is > 15 days and < 30 days'
  properties: {
    backupPolicy: {
      type: 'Periodic'
      periodicModeProperties: {
        backupRetentionIntervalInHours: 500
      }
    }
  }
}
