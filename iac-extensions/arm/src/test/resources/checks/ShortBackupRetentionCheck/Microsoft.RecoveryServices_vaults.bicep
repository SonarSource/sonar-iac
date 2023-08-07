resource noncompliant1 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Sensitive: test case sensitive backupManagementType \'AzureIaasVM\''
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'SimpleRetentionPolicy'
      retentionDuration: {
        count: 7 // Noncompliant{{Make sure that defining a short backup retention duration is safe here.}}
//      ^^^^^^^^
        durationType: 'Days'
//      ^^^^^^^^^^^^^^^^^^^^< {{Duration type}}
      }
    }
  }
}

resource noncompliant2 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Sensitive: test case sensitive backupManagementType \'AzureSql\''
  properties: {
    backupManagementType: 'AzureSql'
    retentionPolicy: {
      retentionPolicyType: 'SimpleRetentionPolicy'
      retentionDuration: {
        count: 7 // Noncompliant
        durationType: 'Days'
      }
    }
  }
}

resource noncompliant3 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Sensitive: test case sensitive backupManagementType \'AzureStorage\''
  properties: {
    backupManagementType: 'AzureStorage'
    retentionPolicy: {
      retentionPolicyType: 'SimpleRetentionPolicy'
      retentionDuration: {
        count: 7 // Noncompliant
        durationType: 'Days'
      }
    }
  }
}

resource noncompliant4 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Sensitive: test case sensitive backupManagementType \'MAB\''
  properties: {
    backupManagementType: 'MAB'
    retentionPolicy: {
      retentionPolicyType: 'SimpleRetentionPolicy'
      retentionDuration: {
        count: 7 // Noncompliant
        durationType: 'Days'
      }
    }
  }
}

resource noncompliant5 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Sensitive: test case sensitive subProtectionPolicy \'GenericProtectionPolicy\''
  properties: {
    backupManagementType: 'GenericProtectionPolicy'
    subProtectionPolicy: [
      {
        retentionPolicy: {
          retentionPolicyType: 'SimpleRetentionPolicy'
          retentionDuration: {
            count: 7 // Noncompliant
            durationType: 'Days'
          }
        }
      }
    ]
  }
}

resource noncompliant6 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Sensitive: test case sensitive subProtectionPolicy \'AzureWorkload\''
  properties: {
    backupManagementType: 'AzureWorkload'
    subProtectionPolicy: [
      {
        retentionPolicy: {
          retentionPolicyType: 'SimpleRetentionPolicy'
          retentionDuration: {
            count: 7 // Noncompliant
            durationType: 'Days'
          }
        }
      }
    ]
  }
}

resource noncompliant7 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Sensitive: test case with retentionPolicyType=LongTermRetentionPolicy'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 7 // Noncompliant
          durationType: 'Days'
        }
      }
    }
  }
}

resource noncompliant8 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Sensitive: durationType in days with less than 30 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 15 // Noncompliant
          durationType: 'Days'
        }
      }
    }
  }
}

resource noncompliant9 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Sensitive: durationType in weeks with less than 30 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 4 // Noncompliant
          durationType: 'Weeks'
        }
      }
    }
  }
}

resource noncompliant10 'Microsoft.RecoveryServices/vaults@2023-01-01' = {
  name: 'Sensitive inner child case'
  resource Microsoft_RecoveryServices_vaults_backupPolicies_Sensitive_inner_child_case_Inner_child 'backupPolicies@2023-01-01' = {
    name: 'Inner child'
    properties: {
      backupManagementType: 'GenericProtectionPolicy'
      subProtectionPolicy: [
        {
          retentionPolicy: {
            retentionPolicyType: 'LongTermRetentionPolicy'
            dailySchedule: {
              retentionDuration: {
                count: 1 // Noncompliant
                durationType: 'Days'
              }
            }
          }
        }
      ]
    }
  }
}

resource compliant1 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: durationType in days with more than 30 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 50
          durationType: 'Days'
        }
      }
    }
  }
}

resource compliant2 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: durationType in days with exactly 30 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 30
          durationType: 'Days'
        }
      }
    }
  }
}

resource compliant3 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: durationType in weeks with more than 30 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 5
          durationType: 'Weeks'
        }
      }
    }
  }
}

resource compliant4 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: durationType in months with more than 30 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 1
          durationType: 'Months'
        }
      }
    }
  }
}

resource compliant5 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: durationType in years with more than 30 days'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 1
          durationType: 'Years'
        }
      }
    }
  }
}

resource compliant6 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: test case backupManagementType with value \'unknown\''
  properties: {
    backupManagementType: 'unknown'
    retentionPolicy: {
      retentionPolicyType: 'SimpleRetentionPolicy'
      retentionDuration: {
        count: 7
        durationType: 'Days'
      }
    }
  }
}

resource compliant7 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: test case retentionPolicyType with value \'unknown\''
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'unknown'
      retentionDuration: {
        count: 7
        durationType: 'Days'
      }
      dailySchedule: {
        retentionDuration: {
          count: 7
          durationType: 'Days'
        }
      }
    }
  }
}

resource compliant8 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: durationType is unknown'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'LongTermRetentionPolicy'
      dailySchedule: {
        retentionDuration: {
          count: 0
          durationType: 'unknown'
        }
      }
    }
  }
}

resource compliant9 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: test case subProtectionPolicy \'unknown\''
  properties: {
    backupManagementType: 'unknown'
    subProtectionPolicy: [
      {
        retentionPolicy: {
          retentionPolicyType: 'LongTermRetentionPolicy'
          dailySchedule: {
            retentionDuration: {
              count: 7
              durationType: 'Days'
            }
          }
        }
      }
    ]
  }
}

resource compliant10 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: durationType property is missing'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'SimpleRetentionPolicy'
      retentionDuration: {
        count: 7
      }
    }
  }
}

resource compliant11 'Microsoft.RecoveryServices/vaults/backupPolicies@2023-01-01' = {
  name: 'Compliant: count property is missing'
  properties: {
    backupManagementType: 'AzureIaasVM'
    retentionPolicy: {
      retentionPolicyType: 'SimpleRetentionPolicy'
      retentionDuration: {
        durationType: 'Days'
      }
    }
  }
}

resource compliant12 'Microsoft.RecoveryServices/vaults@2023-01-01' = {
  name: 'compliant inner child case'
  resource Microsoft_RecoveryServices_vaults_backupPolicies_Sensitive_inner_child_case_Inner_child 'backupPolicies@2023-01-01' = {
    name: 'Compliant inner child case/Inner child'
    properties: {
      backupManagementType: 'GenericProtectionPolicy'
      subProtectionPolicy: [
        {
          retentionPolicy: {
            retentionPolicyType: 'LongTermRetentionPolicy'
            dailySchedule: {
              retentionDuration: {
                count: 50
                durationType: 'Days'
              }
            }
          }
        }
      ]
    }
  }
}
