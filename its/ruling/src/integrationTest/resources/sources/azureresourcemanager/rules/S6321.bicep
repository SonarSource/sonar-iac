resource compliant 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Compliant: valid sourceAddressPrefixes'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefixes: [
      '192.168.1.1'
    ]
  }
}

resource nonCompliant 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case sourceAddressPrefix values'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefix: '0.0.0.0/0'
  }
}
