// Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
resource raise_an_issue 'Microsoft.Network/virtualNetworks@2022-11-01' = {
//                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
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
resource raise_an_issue_check_on_multiple_elements_per_list 'Microsoft.Network/virtualNetworks@2022-11-01' = {
//                                                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
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

resource microsoft_Network_virtualNetworks_Compliant 'Microsoft.Network/virtualNetworks@2022-11-01' = {
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

resource microsoft_Network_virtualNetworks_Compliant 'Microsoft.Network/virtualNetworks@2022-11-01' = {
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
