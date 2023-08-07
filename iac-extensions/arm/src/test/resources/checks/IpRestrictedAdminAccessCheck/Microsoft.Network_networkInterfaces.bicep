// Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
resource raiseAnIssue 'Microsoft.Network/networkInterfaces@2022-11-01' = {
//                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'Raise an issue'
  properties: {
    ipConfigurations: [
      {
        properties: {
          subnet: {
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
        }
      }
    ]
  }
}

resource microsoftNetworkNetworkInterfacesCompliant 'Microsoft.Network/networkInterfaces@2022-11-01' = {
  name: 'Compliant'
  properties: {
    ipConfigurations: [
      {
        properties: {
          subnet: {
            properties: {
              networkSecurityGroup: {
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
                  ]
                }
              }
            }
          }
        }
      }
    ]
  }
}

resource microsoftNetworkNetworkInterfacesCompliant 'Microsoft.Network/networkInterfaces@2022-11-01' = {
  name: 'Compliant'
  properties: {
    ipConfigurations: [
      {
        properties: {
          subnet: {
            properties: {
              networkSecurityGroup: {
                properties: {
                  securityRules: [
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
        }
      }
    ]
  }
}
