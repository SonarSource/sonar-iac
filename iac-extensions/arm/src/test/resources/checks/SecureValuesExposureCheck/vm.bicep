@secure()
param adminUsername string = newGuid() // This parameter is used instead

resource vmExample 'Microsoft.Compute/virtualMachines@2023-03-01' = {
  name: 'vm-example'
  location: 'northeurope'
  properties: {
    osProfile: {
      computerName: 'vm-example'
      adminUsername: adminUsername
    }
  }
}
