// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raise_an_issue_sensitive_case_sourceAddressPrefix_values_1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
//                                                                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
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
resource raise_an_issue_sensitive_case_sourceAddressPrefix_values_2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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
resource raise_an_issue_sensitive_case_sourceAddressPrefix_values_3 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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
resource raise_an_issue_sensitive_case_sourceAddressPrefix_values_4 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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
resource raise_an_issue_sensitive_case_sourceAddressPrefixes_1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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
resource raise_an_issue_sensitive_case_sourceAddressPrefixes_2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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
resource raise_an_issue_sensitive_case_sourceAddressPrefixes_3 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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

resource compliant_valid_sourceAddressPrefixes_1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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

resource compliant_valid_sourceAddressPrefixes_2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Compliant: valid sourceAddressPrefixes 2'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefixes: []
  }
}

resource compliant_not_an_array_sourceAddressPrefixes_3 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Compliant: not an array sourceAddressPrefixes 3'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefixes: 5
  }
}
