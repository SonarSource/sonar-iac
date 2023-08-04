// Noncompliant@+1
resource raiseAnIssueSensitiveCaseProtocolValues1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case protocol values 1'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '22'
    sourceAddressPrefix: 'Internet'
  }
}

// Noncompliant@+1
resource raiseAnIssueSensitiveCaseProtocolValues2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case protocol values 2'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'tcp'
    destinationPortRange: '22'
    sourceAddressPrefix: 'Internet'
  }
}

// Noncompliant@+1
resource raiseAnIssueSensitiveCaseProtocolValues3 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case protocol values 3'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: '*'
    destinationPortRange: '22'
    sourceAddressPrefix: 'Internet'
  }
}

resource compliantProtocolValues1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Compliant: protocol values 1'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'UDP'
    destinationPortRange: '22'
    sourceAddressPrefix: 'Internet'
  }
}

resource compliantProtocolValues2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Compliant: protocol values 2'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: ''
    destinationPortRange: '22'
    sourceAddressPrefix: 'Internet'
  }
}
