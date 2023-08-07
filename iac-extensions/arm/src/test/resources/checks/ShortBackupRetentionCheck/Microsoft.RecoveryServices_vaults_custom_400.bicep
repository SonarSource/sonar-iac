resource noncompliant1 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Sensitive: durationType in days with less than 400 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 10 // Noncompliant
          durationType: 'Days'
        }
      }
    }
  }
}

resource noncompliant2 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Sensitive: durationType in weeks with less than 400 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 10 // Noncompliant
          durationType: 'Weeks'
        }
      }
    }
  }
}

resource noncompliant3 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Sensitive: durationType in months with less than 400 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 5 // Noncompliant
          durationType: 'Months'
        }
      }
    }
  }
}

resource noncompliant4 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Sensitive: durationType in years with less than 400 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 1 // Noncompliant
          durationType: 'Years'
        }
      }
    }
  }
}

resource compliant1 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: durationType in days with more than 400 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 500
          durationType: 'Days'
        }
      }
    }
  }
}

resource compliant2 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: durationType in days with exactly 400 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 400
          durationType: 'Days'
        }
      }
    }
  }
}

resource compliant3 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: durationType in weeks with more than 400 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 70
          durationType: 'Weeks'
        }
      }
    }
  }
}

resource compliant4 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: durationType in months with more than 400 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 24
          durationType: 'Months'
        }
      }
    }
  }
}

resource compliant5 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: durationType in years with more than 400 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 2
          durationType: 'Years'
        }
      }
    }
  }
}
