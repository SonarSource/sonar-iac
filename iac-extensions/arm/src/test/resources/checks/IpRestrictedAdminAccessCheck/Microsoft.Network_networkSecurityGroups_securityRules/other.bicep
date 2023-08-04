resource doNotRaiseAnIssueNotTheConcernedType 'unknown/type/no/issue/in/this/case@2022-11-01' = {
  name: 'Do not raise an issue: not the concerned type'
  properties: {
    direction: 'Inbound'
    access: 'Allow'
    protocol: 'TCP'
    destinationPortRange: '*'
    sourceAddressPrefix: '*'
  }
}
