// Noncompliant@+1{{Restrict IP addresses authorized to access administration services.}}
resource raise_an_issue_sensitive_case_destinationPortRange_values_1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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
resource raise_an_issue_sensitive_case_destinationPortRange_values_2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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
resource raise_an_issue_sensitive_case_destinationPortRange_values_3 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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
resource raise_an_issue_sensitive_case_destinationPortRange_values_4 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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
resource raise_an_issue_sensitive_case_destinationPortRange_values_5 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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
resource raise_an_issue_sensitive_case_destinationPortRange_values_6 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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
resource raise_an_issue_sensitive_case_destinationPortRanges_values_1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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
resource microsoft_Network_networkSecurityGroups_securityRules_Raise_an_issue_sensitive_case_destinationPortRanges_values_2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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
resource microsoft_Network_networkSecurityGroups_securityRules_Raise_an_issue_sensitive_case_destinationPortRanges_values_2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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

resource microsoft_Network_networkSecurityGroups_securityRules_Compliant_destinationPortRanges_is_not_sensitive_1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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

resource microsoft_Network_networkSecurityGroups_securityRules_Compliant_destinationPortRanges_is_not_sensitive_1 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
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

resource compliant_destinationPortRanges_is_not_sensitive_2 'Microsoft.Network/networkSecurityGroups/securityRules@2022-11-01' = {
  name: 'Compliant: destinationPortRanges is not sensitive 2'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRanges: []
    sourceAddressPrefix: 'Internet'
  }
}
