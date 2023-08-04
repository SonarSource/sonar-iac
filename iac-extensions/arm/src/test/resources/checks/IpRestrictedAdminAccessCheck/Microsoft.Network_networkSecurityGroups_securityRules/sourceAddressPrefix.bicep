// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseSourceAddressPrefixValues1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
//                                                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'Raise an issue: sensitive case sourceAddressPrefix values 1'
  properties: {
    direction: 'Inbound'
//             ^^^^^^^^^< {{Sensitive direction}}
    access: 'Allow'
    //      ^^^^^^^< {{Sensitive access}}
    protocol: 'TCP'
    //        ^^^^^< {{Sensitive protocol}}
    destinationPortRange: '*'
    //                    ^^^< {{Sensitive destination port range}}
    sourceAddressPrefix: '*'
    //                   ^^^< {{Sensitive source address prefix}}
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseSourceAddressPrefixValues2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case sourceAddressPrefix values 2'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefix: '0.0.0.0/0'
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseSourceAddressPrefixValues3 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case sourceAddressPrefix values 3'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefix: '::/0'
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseSourceAddressPrefixValues4 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case sourceAddressPrefix values 4'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefix: 'Internet'
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseSourceAddressPrefixes1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case sourceAddressPrefixes 1'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefixes: [
      'Internet'
    ]
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseSourceAddressPrefixes2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case sourceAddressPrefixes 2'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefixes: [
      '192.168.1.1'
      'Internet'
    ]
  }
}

// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueSensitiveCaseSourceAddressPrefixes3 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Raise an issue: sensitive case sourceAddressPrefixes 3'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefixes: [
      '*'
      'Internet'
    ]
  }
}

resource compliantValidSourceAddressPrefixes1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Compliant: valid sourceAddressPrefixes 1'
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

resource compliantValidSourceAddressPrefixes2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Compliant: valid sourceAddressPrefixes 2'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefixes: []
  }
}

resource compliantNotAnArraySourceAddressPrefixes3 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Compliant: not an array sourceAddressPrefixes 3'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefixes: 5
  }
}
