@secure()
param adminUsername string = newGuid() // Unused in this example

module compliantModuleExample 'vm.bicep' = { // Compliant: Bicep modules always use the correct scope
  name: 'module-bicep-compliant'
}
