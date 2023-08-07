resource noncompliant1 'Microsoft.DocumentDB/databaseAccounts@2023-04-15' = {
  name: 'Sensitive: properties.backupPolicy.type is Periodic and backupRetentionIntervalInHours / 24 < 30 days'
  properties: {
    backupPolicy: {
      type: 'Periodic'
      periodicModeProperties: {
        backupRetentionIntervalInHours: 24 // Noncompliant{{Make sure that defining a short backup retention duration is safe here.}}
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      }
    }
  }
}

resource noncompliant2 'Microsoft.DocumentDB/databaseAccounts@2023-04-15' = {
  name: 'Sensitive: properties.backupPolicy.type is Periodic and backupRetentionIntervalInHours is not set (default 8 hour)'
  properties: {
    backupPolicy: {
      type: 'Periodic'
      // Noncompliant@+1{{Omitting "backupRetentionIntervalInHours" causes a short backup retention period to be set. Make sure that defining a short backup retention duration is safe here.}}
      periodicModeProperties: {}
//                            ^^
    }
  }
}

resource noncompliant3 'Microsoft.DocumentDB/databaseAccounts@2023-04-15' = {
  name: 'Sensitive: properties.backupPolicy.type is Periodic and not even periodicModeProperties is set (still default to 8 hour)'
  properties: {
    // Noncompliant@+1{{Omitting "periodicModeProperties.backupRetentionIntervalInHours" causes a short backup retention period to be set. Make sure that defining a short backup retention duration is safe here.}}
    backupPolicy: {
      type: 'Periodic'
    }
  }
}

resource noncompliant4 'Microsoft.DocumentDB/databaseAccounts@2023-04-15' = {
  name: 'Sensitive: backupRetentionIntervalInHours is 719, which is just below the 720 hours (30 days) threshold'
  properties: {
    backupPolicy: {
      type: 'Periodic'
      periodicModeProperties: {
        backupRetentionIntervalInHours: 719 // Noncompliant
      }
    }
  }
}

resource compliant1 'Microsoft.DocumentDB/databaseAccounts@2023-04-15' = {
  name: 'Compliant: no attributes are set'
  properties: {
    backupPolicy: {}
  }
}

resource compliant2 'Microsoft.DocumentDB/databaseAccounts@2023-04-15' = {
  name: 'Compliant: properties.backupPolicy.type is not \'Periodic\''
  properties: {
    backupPolicy: {
      type: 'Other'
      periodicModeProperties: {
        backupRetentionIntervalInHours: 24
      }
    }
  }
}

resource compliant3 'Microsoft.DocumentDB/databaseAccounts@2023-04-15' = {
  name: 'Compliant: backupRetentionIntervalInHours / 24 > 30'
  properties: {
    backupPolicy: {
      type: 'Periodic'
      periodicModeProperties: {
        backupRetentionIntervalInHours: 1000
      }
    }
  }
}

resource compliant4 'Microsoft.DocumentDB/databaseAccounts@2023-04-15' = {
  name: 'Compliant: backupRetentionIntervalInHours is 720, which is just below the 720 hours (30 days) threshold'
  properties: {
    backupPolicy: {
      type: 'Periodic'
      periodicModeProperties: {
        backupRetentionIntervalInHours: 720
      }
    }
  }
}

resource compliant5 'Microsoft.DocumentDB/databaseAccounts@2023-04-15' = {
  name: 'Compliant: type is not set'
  properties: {
    backupPolicy: {
      periodicModeProperties: {
        backupRetentionIntervalInHours: 24
      }
    }
  }
}
