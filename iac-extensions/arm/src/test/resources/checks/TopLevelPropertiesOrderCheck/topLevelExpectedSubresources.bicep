resource virtualNetwork 'Microsoft.Network/virtualNetworks@2019-11-01' = {
  name: virtualNetworkName
  location: location

  resource subnet1 'subnets' existing = {
    name: subnet1Name
  }

  resource subnet2 'subnets' existing = {
    name: subnet2Name
  }
}
