resource noncompliant 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'backup'
  properties: {
    __sonar__: 'Sensitive: retention period is less than 30'
    backupSchedule: {
      retentionPeriodInDays: 7
    }
  }
}

resource compliant 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'backup'
  properties: {
    backupSchedule: {
      retentionPeriodInDays: 35
    }
  }
}
