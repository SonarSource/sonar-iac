resource flowLogs1 'Microsoft.Network/firewallPolicies@2023-02-01' = {
  name: 'Noncompliant: Raise Issue as retentionDays are too low with custom value'
  properties: {
    insights: {
      // Noncompliant@+1 {{Make sure that defining a short log retention duration is safe here.}}
      retentionDays: 29
      isEnabled: true
    }
  }
}

resource flowLogs2 'Microsoft.Network/firewallPolicies@2023-02-01' = {
  name: 'Compliant'
  properties: {
    insights: {
      retentionDays: 30
      isEnabled: true
    }
  }
}
