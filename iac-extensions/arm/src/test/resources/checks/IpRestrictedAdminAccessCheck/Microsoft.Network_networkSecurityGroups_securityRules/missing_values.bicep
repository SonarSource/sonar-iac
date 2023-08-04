resource doNotRaiseAnIssueMissingDirection 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Do not raise an issue: missing direction'
  properties: {
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '22'
    sourceAddressPrefix: 'Internet'
  }
}

resource doNotRaiseAnIssueMissingAccess 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Do not raise an issue: missing access'
  properties: {
    direction: 'Inbound'
    protocol: 'TCP'
    destinationPortRange: '22'
    sourceAddressPrefix: 'Internet'
  }
}

resource doNotRaiseAnIssueMissingProtocol 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Do not raise an issue: missing protocol'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    destinationPortRange: '22'
    sourceAddressPrefix: 'Internet'
  }
}

resource doNotRaiseAnIssueMissingDestinationPortRange 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Do not raise an issue: missing destinationPortRange'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    sourceAddressPrefix: 'Internet'
  }
}

resource doNotRaiseAnIssueMissingSourceAddressPrefix 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Do not raise an issue: missing sourceAddressPrefix'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '22'
  }
}
