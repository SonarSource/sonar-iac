// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseDestinationPortRangeValues1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case destinationPortRange values 1'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefix: 'Internet'
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseDestinationPortRangeValues2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case destinationPortRange values 2'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '22'
    sourceAddressPrefix: 'Internet'
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseDestinationPortRangeValues3 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case destinationPortRange values 3'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '3389'
    sourceAddressPrefix: 'Internet'
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseDestinationPortRangeValues4 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case destinationPortRange values 4'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '10-22'
    sourceAddressPrefix: 'Internet'
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseDestinationPortRangeValues5 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case destinationPortRange values 5'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '1-5000'
    sourceAddressPrefix: 'Internet'
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseDestinationPortRangeValues6 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case destinationPortRange values 6'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '3000-4000'
    sourceAddressPrefix: 'Internet'
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseDestinationPortRangesValues1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case destinationPortRanges values 1'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRanges: [
      '*'
    ]
    sourceAddressPrefix: 'Internet'
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource microsoftNetworkNetworkSecurityGroupsSecurityRulesRaiseAnIssueSensitiveCaseDestinationPortRangesValues2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case destinationPortRanges values 2'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRanges: [
      '*'
      '22'
    ]
    sourceAddressPrefix: 'Internet'
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource microsoftNetworkNetworkSecurityGroupsSecurityRulesRaiseAnIssueSensitiveCaseDestinationPortRangesValues2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case destinationPortRanges values 2'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRanges: [
      '80'
      '22'
    ]
    sourceAddressPrefix: 'Internet'
  }
}

resource microsoftNetworkNetworkSecurityGroupsSecurityRulesCompliantDestinationPortRangesIsNotSensitive1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Compliant: destinationPortRanges is not sensitive 1'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRanges: [
      '80'
    ]
    sourceAddressPrefix: 'Internet'
  }
}

resource microsoftNetworkNetworkSecurityGroupsSecurityRulesCompliantDestinationPortRangesIsNotSensitive1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Compliant: destinationPortRanges is not sensitive 1'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRanges: [
      '10-20'
      '50-80'
    ]
    sourceAddressPrefix: 'Internet'
  }
}

resource compliantDestinationPortRangesIsNotSensitive2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Compliant: destinationPortRanges is not sensitive 2'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRanges: []
    sourceAddressPrefix: 'Internet'
  }
}
