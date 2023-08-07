// Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssue 'Microsoft.Network/networkSecurityGroup@2022-11-01' = {
  name: 'Raise an issue'
  properties: {
    securityRules: [
      {
        properties: {
          direction: 'Inbound'
          access: 'Allow'
          protocol: 'TCP'
          destinationPortRange: '*'
          sourceAddressPrefix: '*'
        }
      }
    ]
  }
}

// Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}} {{Restrict IP addresses authorized to access administration services.}}
resource raise2IssuesWithEachHavingSameMainLocationButDifferentSecondaryLocations 'Microsoft.Network/networkSecurityGroup@2022-11-01' = {
//                                                                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'Raise 2 issues, with each having same main location but different secondary locations'
  properties: {
    securityRules: [
      {
        properties: {
          direction: 'Inbound'
          //         ^^^^^^^^^< {{Sensitive direction}}
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
      {
        properties: {
          direction: 'Inbound'
          access: 'Allow'
          protocol: 'TCP'
          destinationPortRange: '*'
          sourceAddressPrefix: '*'
        }
      }
    ]
  }
}

// Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
resource raiseIssueOnSecondSecurityRuleOnly 'Microsoft.Network/networkSecurityGroup@2022-11-01' = {
//                                           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'Raise issue on second security rule only'
  properties: {
    securityRules: [
      {
        properties: {
          direction: 'Inbound'
          access: 'Allow'
          protocol: 'UDP'
          destinationPortRange: '*'
          sourceAddressPrefix: '*'
        }
      }
      {
        properties: {
          direction: 'Inbound'
          //         ^^^^^^^^^< {{Sensitive direction}}
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
    ]
  }
}

resource microsoftNetworkNetworkSecurityGroupNoIssueToRaise 'Microsoft.Network/networkSecurityGroup@2022-11-01' = {
  name: 'No issue to raise'
  properties: {
    securityRules: []
  }
}

resource microsoftNetworkNetworkSecurityGroupNoIssueToRaise 'Microsoft.Network/networkSecurityGroup@2022-11-01' = {
  name: 'No issue to raise'
  properties: {
    securityRules: [
      {
        properties: {}
      }
    ]
  }
}

resource noIssueToRaiseNoArrayExpressionWhereExpected 'Microsoft.Network/networkSecurityGroup@2022-11-01' = {
  name: 'No issue to raise: no ArrayExpression where expected'
  properties: {
    securityRules: {
      properties: {
        direction: 'Inbound'
        access: 'Allow'
        protocol: 'TCP'
        destinationPortRange: '*'
        sourceAddressPrefix: '*'
      }
    }
  }
}
