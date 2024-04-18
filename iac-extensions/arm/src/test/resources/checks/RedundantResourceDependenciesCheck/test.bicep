resource nonCompliant1 'Microsoft.Network/networkInterfaces@2020-06-01' = {
  name: 'nonCompliant1'
  location: resourceGroup().location
  dependsOn: [
    exampleSubnet // Noncompliant{{Remove this explicit dependency as it is already defined implicitly.}}
//  ^^^^^^^^^^^^^
    validExplicitDependency
  ]
  properties: {
    ipConfigurations: [
      {
        name: 'ipconfig1'
        properties: {
          subnet: {
            id: exampleSubnet.id
//              ^^^^^^^^^^^^^< {{Implicit dependency is created via a symbolic name.}}
          }
        }
      }
      {
        name: 'ipconfig2'
        properties: {
          subnet: {
            id: reference('exampleSubnet').id
//                        ^^^^^^^^^^^^^^^< {{Implicit dependency is created via the "reference" function.}}
          }
        }
      }
    ]
  }
}

resource nonCompliant2 'Microsoft.Network/networkInterfaces@2020-06-01' = {
  name: 'nonCompliant2'
  location: resourceGroup().location
  dependsOn: [
    resourceId('Microsoft.Network/virtualNetworks', 'exampleSubnet') // Noncompliant
//                                                  ^^^^^^^^^^^^^^^
  ]
  properties: {
    ipConfigurations: [
      {
        name: 'ipconfig1'
        properties: {
          subnet: {
            id: exampleSubnet.id
//              ^^^^^^^^^^^^^<
          }
        }
      }
      {
        name: 'ipconfig2'
        properties: {
          subnet: {
            id: reference('exampleSubnet').id
//                        ^^^^^^^^^^^^^^^<
          }
        }
      }
    ]
  }
}

resource publicIPAddresses 'Microsoft.Network/publicIPAddresses@2021-05-01' = [for i in range(0, 3): {
  name: '${publicIPAddressName}${i}'
  location: location
}]

resource nonCompliant3 'Microsoft.Network/networkInterfaces@2020-06-01' = {
  dependsOn: [
    publicIPAddresses[0] // Noncompliant
  ]
  properties: {
    name: publicIPAddresses[0].name
  }
}

resource compliant 'Microsoft.Network/networkInterfaces@2020-06-01' = {
  name: 'compliant'
  location: resourceGroup().location
  properties: {
    ipConfigurations: [
      {
        name: 'ipconfig1'
        properties: {
          subnet: {
            id: exampleSubnet.id
          }
        }
      }
    ]
  }
}
