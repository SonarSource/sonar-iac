// Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
resource raise_an_issue 'Microsoft.Network/networkInterfaces@2022-11-01' = {
//                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
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

resource microsoft_Network_networkInterfaces_Compliant 'Microsoft.Network/networkInterfaces@2022-11-01' = {
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

resource microsoft_Network_networkInterfaces_Compliant 'Microsoft.Network/networkInterfaces@2022-11-01' = {
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
