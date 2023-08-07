resource noncompliant 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'backup'
  properties: {
    __sonar__: 'Sensitive: retention period is less than custom period 15'
    backupSchedule: {
      retentionPeriodInDays: 7 // Noncompliant
    }
  }
}

resource compliant 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'backup'
  properties: {
    __sonar__: 'Compliant: retention period is greater than 15'
    backupSchedule: {
      retentionPeriodInDays: 20
    }
  }
}
