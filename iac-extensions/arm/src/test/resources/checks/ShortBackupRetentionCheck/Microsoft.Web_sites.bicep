resource noncompliant1 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'backup'
  properties: {
    __sonar__: 'Sensitive: retention period is less than 30'
    backupSchedule: {
      retentionPeriodInDays: 7 // Noncompliant {{Make sure that defining a short backup retention duration is safe here.}}
//    ^^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource noncompliant2 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 1 > 0 ? 'webApp' : 'backup'
  properties: {
    __sonar__: 'Sensitive: also detect on name != backup, expression could be resolved to backup'
    backupSchedule: {
      retentionPeriodInDays: 7 // Noncompliant
    }
  }
}

resource noncompliant3 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'other random name'
  properties: {
    __sonar__: 'Sensitive: name is not backup'
    backupSchedule: {
      retentionPeriodInDays: 7 // Noncompliant
    }
  }
}

resource noncompliant4 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'backup'
  properties: {
    __sonar__: 'Sensitive: retention period is zero'
    backupSchedule: {
      retentionPeriodInDays: 0 // Noncompliant
    }
  }
}

resource noncompliant5 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'backup'
  properties: {
    __sonar__: 'Sensitive: retention period is negative'
    backupSchedule: {
      retentionPeriodInDays: -5 // Noncompliant
    }
  }
}

resource noncompliant6 'Microsoft.Web/sites@2022-03-01' = {
  name: 'backup'
  resource nested_child 'config@2022-03-01' = {
    name: 'backup'
    properties: {
      backupSchedule: {
        __sonar__: 'Sensitive: sensitive case in nested resource'
        retentionPeriodInDays: 7 // Noncompliant
      }
    }
  }
}

resource compliant1 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'backup'
  properties: {
    backupSchedule: {
      retentionPeriodInDays: 35
    }
  }
}

resource compliant2 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'backup'
  properties: {
    backupSchedule: {
      retentionPeriodInDays: 30
    }
  }
}

resource compliant3 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'backup'
  properties: {
    backupSchedule: {
      retentionPeriodInDays: '14'
    }
  }
}

resource compliant4 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'backup'
  properties: {
    backupSchedule: {
      'something else': '14'
    }
  }
}

resource compliant5 'Microsoft.Web/sites@2022-03-01' = {
  name: 'Compliant: compliant case in nested resource'
  resource compliant6 'config@2022-03-01' = {
  name: 'backup'
    properties: {
      backupSchedule: {
        retentionPeriodInDays: 35
      }
    }
  }
}

resource compliant6 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'backup'
  properties: {
    backupSchedule: {
      retentionPeriodInDays: +35
    }
  }
}

resource compliant7 'Microsoft.Web/sites/config@2022-03-01' = {
  name: 'backup'
  properties: {
    backupSchedule: {
      retentionPeriodInDays: !'str'
    }
  }
}
