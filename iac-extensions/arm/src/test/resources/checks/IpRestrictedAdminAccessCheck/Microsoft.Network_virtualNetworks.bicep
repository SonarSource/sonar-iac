// Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssue 'Microsoft.Network/virtualNetworks@2022-11-01' = {
//                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'Raise an issue'
  properties: {
    subnets: [
      {
        properties: {
          networkSecurityGroup: {
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
              ]
            }
          }
        }
      }
    ]
  }
}

// Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssueCheckOnMultipleElementsPerList 'Microsoft.Network/virtualNetworks@2022-11-01' = {
//                                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'Raise an issue: check on multiple elements per list'
  properties: {
    subnets: [
      {}
      {
        properties: {
          networkSecurityGroup: {
            properties: {
              securityRules: [
                {}
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
        }
      }
    ]
  }
}

resource microsoftNetworkVirtualNetworksCompliant 'Microsoft.Network/virtualNetworks@2022-11-01' = {
  name: 'Compliant'
  properties: {
    subnets: [
      {}
      {
        properties: {
          networkSecurityGroup: {
            properties: {
              securityRules: [
                {}
                {
                  properties: {
                    direction: 'Inbound'
                    access: 'Allow'
                    protocol: 'UDP'
                    destinationPortRange: '*'
                    sourceAddressPrefix: '*'
                  }
                }
              ]
            }
          }
        }
      }
    ]
  }
}

resource microsoftNetworkVirtualNetworksCompliant 'Microsoft.Network/virtualNetworks@2022-11-01' = {
  name: 'Compliant'
  properties: {
    subnets: [
      {}
      {
        properties: {
          networkSecurityGroup: {
            properties: {
              securityRules: [
                {}
                {
                  properties: {
                    direction: 'Inbound'
                    access: 'Allow'
                    destinationPortRange: '*'
                    sourceAddressPrefix: '*'
                  }
                }
              ]
            }
          }
        }
      }
    ]
  }
}
