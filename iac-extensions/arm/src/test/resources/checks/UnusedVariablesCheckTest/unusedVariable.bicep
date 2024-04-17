var unusedString = 'bar'               // Noncompliant {{Remove the unused variable "unusedString".}}
//  ^^^^^^^^^^^^
var unusedBool = true                  // Noncompliant {{Remove the unused variable "unusedBool".}}
//  ^^^^^^^^^^
var unusedInt = 2                      // Noncompliant {{Remove the unused variable "unusedInt".}}
//  ^^^^^^^^^
var unusedArray = ['val']              // Noncompliant {{Remove the unused variable "unusedArray".}}
//  ^^^^^^^^^^^
var unusedObject = [                   // Noncompliant {{Remove the unused variable "unusedObject".}}
//  ^^^^^^^^^^^^
  {
    key: 'foo'
  }
]
var unusedVariable = 'bar' // Noncompliant {{Remove the unused variable "unusedVariable".}}

var usedInResourceProperties = usedInOtherVar
var usedInOtherVar = 'bar'
var usedInsideUserAssignedIdentities = 'bar'
var usedInResourceName = 'bar'
var usedInResourceLocation = 'bar'
var usedInResourceKind = 'bar'
var usedInResourceTag = 'bar'
var usedInResourceDependsOn = 'bar'
var usedInResourceOtherKey = 'bar'
var usedInResourceOtherFoo = 'bar'
var usedInChildResource = 'bar'

var usedInParamString = 'bar'
var usedInParamBool = true
var usedInParamInt = 2
var usedInParamArray = ['val']
var usedInParamObject = [
  {
    key: 'foo'
  }
]

var usedInOutputString = 'bar'
var usedInOutputBool = true
var usedInOutputInt = 2
var usedInOutputArray = ['val']
var usedInOutputObject = [
  {
    key: 'foo'
  }
]
var usedInOutputFor = 5
var usedInOutputFor = 'bar'
var usedInOutput2 = 'bar'
var usedInOutput3 = 'bar'
var usedCaseInsensitive = 'bar'

resource exampleStorage 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: usedInResourceName
  kind: usedInResourceKind
  location: '${usedInResourceLocation}ConcatToVariable'
  tags: {
    tag1: usedInResourceTag
  }
  dependsOn: [
    usedInResourceDependsOn
  ]
  unusedVariable: 'foo'
  caseInsensitive: UseDcaseInSensitivE
  properties: {
    unusedVariable: usedInResourceProperties
  }
  other: [
    {
      key: usedInResourceOtherKey
      something: '${toLower(usedInResourceOtherFoo)}ConcatToVariable'
      unusedVariable: 'foo'
    }
  ]
  identity: {
      type: 'UserAssigned'
      userAssignedIdentities: {
        '${usedInsideUserAssignedIdentities}': {}
      }
    }
  resource service 'fileServices' = {
    unusedVariable: usedInChildResource
  }
}

param par1 string = usedInParamObject.key
param par2 string = usedInParamString
param par3 int = usedInParamInt
param par4 array = usedInParamArray
param par5 bool = usedInParamBool

output out1 object = usedInOutputObject
output out2 string = usedInOutputString
output out3 int = usedInOutputInt
output out4 array = usedInOutputArray
output out5 bool = usedInOutputBool
output out6 string = bar[usedInOutput2]
output out7 string = usedInOutput3['foo']

output outFor array = [for i in range(0, usedInOutputFor): {
  name: usedInOutputForInner
}]

output outputStillUnused string = usedInOutput3['unusedVariable'].unusedVariable
output outputStillUnused2 string = usedInOutput3['unusedVariable'].unusedVariable()

output outputStillUnused3 array = [for i in range(0, 5): {
  unusedVariable: 'foo'
}]
