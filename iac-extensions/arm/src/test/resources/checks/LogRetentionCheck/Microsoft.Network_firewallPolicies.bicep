resource firewallPolicy1 'Microsoft.Network/firewallPolicies@2023-02-01' = {
  name: 'Noncompliant: Raise Issue as retentionDays are too low'
  properties: {
    insights: {
      isEnabled: true
      // Noncompliant@+1 {{Make sure that defining a short log retention duration is safe here.}}
      retentionDays: 7
    }
  }
}

resource firewallPolicy2 'Microsoft.Network/firewallPolicies@2023-02-01' = {
  name: 'Noncompliant: Raise Issue as retentionDays are missing'
  properties: {
    // Noncompliant@+1 {{Omitting "retentionDays" results in a short log retention duration. Make sure it is safe here.}}
    insights: {
      isEnabled: true
    }
  }
}

resource firewallPolicy3 'Microsoft.Network/firewallPolicies@2023-02-01' = {
  name: 'Noncompliant: Raise Issue as isEnabled is missing'
  properties: {
    // Noncompliant@+1 {{Omitting "isEnabled" results in a short log retention duration. Make sure it is safe here.}}
    insights: {
      retentionDays: 15
    }
  }
}

resource firewallPolicy4 'Microsoft.Network/firewallPolicies@2023-02-01' = {
  name: 'Noncompliant: Raise Issue as retentionDays are too low and isEnabled is false'
  properties: {
    insights: {
      // Noncompliant@+1 {{Disabling "isEnabled" results in a short log retention duration. Make sure it is safe here.}}
      isEnabled: false
      // Noncompliant@+1 {{Make sure that defining a short log retention duration is safe here.}}
      retentionDays: 7
    }
  }
}

resource firewallPolicy5 'Microsoft.Network/firewallPolicies@2023-02-01' = {
  name: 'Noncompliant: Raise Issue as isEnabled is false'
  properties: {
    insights: {
      // Noncompliant@+1 {{Disabling "isEnabled" results in a short log retention duration. Make sure it is safe here.}}
      isEnabled: false
      retentionDays: 15
    }
  }
}

// Noncompliant@+1 {{Omitting "insights" results in a short log retention duration. Make sure it is safe here.}}
resource firewallPolicy6 'Microsoft.Network/firewallPolicies@2023-02-01' = {
  name: 'Noncompliant: Raise Issue as insights is missing'
  properties: {
  }
}

resource firewallPolicy7 'Microsoft.Network/firewallPolicies@2023-02-01' = {
  name: 'Compliant: isEnabled is true and retentionDays equals 15'
  properties: {
    insights: {
      isEnabled: true
      retentionDays: 15
    }
  }
}

resource firewallPolicy8 'Microsoft.Network/firewallPolicies@2023-02-01' = {
  name: 'Compliant: isEnabled is true and retentionDays equals 0'
  properties: {
    insights: {
      isEnabled: true
      retentionDays: 0
    }
  }
}

resource firewallPolicy9 'Microsoft.Network/firewallPolicies@2023-02-01' = {
  name: 'Compliant: isEnabled and retentionDays are in wrong format'
  properties: {
    insights: {
      isEnabled: 'wrong format'
      retentionDays: 'wrong format'
    }
  }
}

resource firewallPolicy10 'Microsoft.Network/otherType@2023-02-01' = {
  name: 'Compliant: other type'
  properties: {
    insights: {
      isEnabled: true
      retentionDays: 7
    }
  }
}
