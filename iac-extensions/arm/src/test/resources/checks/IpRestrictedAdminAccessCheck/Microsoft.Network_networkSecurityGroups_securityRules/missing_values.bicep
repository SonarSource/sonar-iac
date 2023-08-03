resource do_not_raise_an_issue_missing_direction 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Do not raise an issue: missing direction'
  properties: {
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '22'
    sourceAddressPrefix: 'Internet'
  }
}

resource do_not_raise_an_issue_missing_access 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Do not raise an issue: missing access'
  properties: {
    direction: 'Inbound'
    protocol: 'TCP'
    destinationPortRange: '22'
    sourceAddressPrefix: 'Internet'
  }
}

resource do_not_raise_an_issue_missing_protocol 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Do not raise an issue: missing protocol'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    destinationPortRange: '22'
    sourceAddressPrefix: 'Internet'
  }
}

resource do_not_raise_an_issue_missing_destinationPortRange 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Do not raise an issue: missing destinationPortRange'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    sourceAddressPrefix: 'Internet'
  }
}

resource do_not_raise_an_issue_missing_sourceAddressPrefix 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Do not raise an issue: missing sourceAddressPrefix'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '22'
  }
}
