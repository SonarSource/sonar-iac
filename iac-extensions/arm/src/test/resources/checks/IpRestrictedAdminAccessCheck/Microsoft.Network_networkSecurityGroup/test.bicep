// Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
resource raise_an_issue 'Microsoft.Network/networkSecurityGroup@2022-11-01' = {
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
resource raise_2_issues_with_each_having_same_main_location_but_different_secondary_locations 'Microsoft.Network/networkSecurityGroup@2022-11-01' = {
//                                                                                            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
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
resource raise_issue_on_second_security_rule_only 'Microsoft.Network/networkSecurityGroup@2022-11-01' = {
//                                                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
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

resource microsoft_Network_networkSecurityGroup_No_issue_to_raise 'Microsoft.Network/networkSecurityGroup@2022-11-01' = {
  name: 'No issue to raise'
  properties: {
    securityRules: []
  }
}

resource microsoft_Network_networkSecurityGroup_No_issue_to_raise 'Microsoft.Network/networkSecurityGroup@2022-11-01' = {
  name: 'No issue to raise'
  properties: {
    securityRules: [
      {
        properties: {}
      }
    ]
  }
}

resource no_issue_to_raise_no_ArrayExpression_where_expected 'Microsoft.Network/networkSecurityGroup@2022-11-01' = {
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
