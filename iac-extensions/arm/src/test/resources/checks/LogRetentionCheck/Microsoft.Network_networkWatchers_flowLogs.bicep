resource flowLogs1 'Microsoft.Network/networkWatchers/flowLogs@2022-07-01' = {
  name: 'Noncompliant: Raise Issue as days is too low /'
  properties: {
    storageId: 'dummy'
    targetResourceId: 'dummy'
    retentionPolicy: {
      // Noncompliant@+1 {{Make sure that defining a short log retention duration is safe here.}}
      days: 7
      enabled: true
    }
  }
}

resource flowLogs2 'Microsoft.Network/networkWatchers/flowLogs@2022-07-01' = {
  name: 'Noncompliant: Raise Issue as days is missing /'
  properties: {
    storageId: 'dummy'
    targetResourceId: 'dummy'
    // Noncompliant@+1 {{Omitting "days" results in a short log retention duration. Make sure it is safe here.}}
    retentionPolicy: {
      enabled: true
    }
  }
}

resource flowLogs3 'Microsoft.Network/networkWatchers/flowLogs@2022-07-01' = {
  name: 'Noncompliant: Raise Issue as enabled is missing /'
  properties: {
    storageId: 'dummy'
    targetResourceId: 'dummy'
    // Noncompliant@+1 {{Omitting "enabled" results in a short log retention duration. Make sure it is safe here.}}
    retentionPolicy: {
      days: 15
    }
  }
}

resource flowLogs4 'Microsoft.Network/networkWatchers/flowLogs@2022-07-01' = {
  name: 'Noncompliant: Raise Issue as enabled is false and days is too low /'
  properties: {
    storageId: 'dummy'
    targetResourceId: 'dummy'
    retentionPolicy: {
      // Noncompliant@+1 {{Make sure that defining a short log retention duration is safe here.}}
      days: 7
      // Noncompliant@+1 {{Disabling "enabled" results in a short log retention duration. Make sure it is safe here.}}
      enabled: false
    }
  }
}

resource flowLogs5 'Microsoft.Network/networkWatchers/flowLogs@2022-07-01' = {
  name: 'Noncompliant: Raise Issue as enabled is false /'
  properties: {
    storageId: 'dummy'
    targetResourceId: 'dummy'
    retentionPolicy: {
      days: 15
      // Noncompliant@+1 {{Disabling "enabled" results in a short log retention duration. Make sure it is safe here.}}
      enabled: false
    }
  }
}

// Noncompliant@+1 {{Omitting "retentionPolicy" results in a short log retention duration. Make sure it is safe here.}}
resource flowLogs6 'Microsoft.Network/networkWatchers/flowLogs@2022-07-01' = {
  name: 'Noncompliant: Raise Issue as retentionPolicy is missing /'
  properties: {
    storageId: 'dummy'
    targetResourceId: 'dummy'
  }
}

resource flowLogs7 'Microsoft.Network/networkWatchers@2022-07-01' = {
  name: 'Noncompliant: Raise issue on inner child'

  resource child1 'flowLogs' = {
    name: 'inner child'
    properties: {
      retentionPolicy: {
        enabled: true
        // Noncompliant@+1 {{Make sure that defining a short log retention duration is safe here.}}
        days: 7
      }
      storageId: 'dummy'
      targetResourceId: 'dummy'
    }
  }
}

resource flowLogs8 'Microsoft.Network/networkWatchers/flowLogs@2022-07-01' = {
  name: 'Compliant: is enabled and 15 days /'
  properties: {
    storageId: 'dummy'
    targetResourceId: 'dummy'
    retentionPolicy: {
      days: 15
      enabled: true
    }
  }
}

resource flowLogs9 'Microsoft.Network/networkWatchers/flowLogs@2022-07-01' = {
  name: 'Compliant: is enabled and 0 days (no limit) /'
  properties: {
    storageId: 'dummy'
    targetResourceId: 'dummy'
    retentionPolicy: {
      days: 0
      enabled: true
    }
  }
}

resource flowLogs10 'Microsoft.Network/networkWatchers/flowLogs@2022-07-01' = {
  name: 'Compliant: wrong format /'
  properties: {
    storageId: 'dummy'
    targetResourceId: 'dummy'
    retentionPolicy: {
      days: 0
      enabled: true
    }
  }
}
