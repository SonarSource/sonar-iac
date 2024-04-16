param unusedString string = 'bar'               // Noncompliant {{Remove the unused parameter "unusedString".}}
//    ^^^^^^^^^^^^
param unusedBool bool = true                  // Noncompliant {{Remove the unused parameter "unusedBool".}}
//    ^^^^^^^^^^
param unusedInt int = 2                      // Noncompliant {{Remove the unused parameter "unusedInt".}}
//    ^^^^^^^^^
param unusedArray array = ['val']              // Noncompliant {{Remove the unused parameter "unusedArray".}}
//    ^^^^^^^^^^^
param unusedObject object = [                   // Noncompliant {{Remove the unused parameter "unusedObject".}}
//    ^^^^^^^^^^^^
  {
    key: 'foo'
  }
]
param unusedParameter string = 'bar' // Noncompliant {{Remove the unused parameter "unusedParameter".}}

param usedInResourceProperties string = usedInOtherVar
param usedInOtherVar string = 'bar'
param usedInResourceName string
param usedInResourceLocation string = 'bar'
param usedInResourceKind string = 'bar'
param usedInResourceTag string = 'bar'
param usedInResourceDependsOn string = 'bar'
param usedInResourceOtherKey string = 'bar'
param usedInResourceOtherFoo string = 'bar'
param usedInChildResource string = 'bar'

param usedInVariableString string = 'bar'
param usedInVariableBool bool = true
param usedInVariableInt int = 2
param usedInVariableArray array = ['val']
param usedInVariableObject object = [
  {
    key: 'foo'
  }
]

param usedInOutputString string = 'bar'
param usedInOutputBool bool = true
param usedInOutputInt int = 2
param usedInOutputArray array = ['val']
param usedInOutputObject object
param usedInOutputFor int = 5
param usedInOutputFor string = 'bar'
param usedInOutput2 string = 'bar'
param usedInOutput3 string = 'bar'
param usedCaseInsensitive string = 'bar'

resource exampleStorage 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: usedInResourceName
  kind: usedInResourceKind
  sku: undefinedParameter
  location: '${usedInResourceLocation}ConcatToParameter'
  tags: {
    tag1: usedInResourceTag
  }
  dependsOn: [
    usedInResourceDependsOn
  ]
  unusedParameter: 'foo'
  caseInsensitive: UseDcaseInSensitivE
  properties: {
    unusedParameter: usedInResourceProperties
  }
  other: [
    {
      key: usedInResourceOtherKey
      something: '${toLower(usedInResourceOtherFoo)}ConcatToParameter'
      unusedParameter: 'foo'
    }
  ]
  resource service 'fileServices' = {
    unusedParameter: usedInChildResource
  }
}

var var1 = usedInVariableObject.key
var var2 = usedInVariableString
var var3 = usedInVariableInt
var var4 = usedInVariableArray
var var5 = usedInVariableBool

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

output outputStillUnused string = usedInOutput3['unusedParameter'].unusedParameter
output outputStillUnused2 string = usedInOutput3['unusedParameter'].unusedParameter()

output outputStillUnused3 array = [for i in range(0, 5): {
  unusedParameter: 'foo'
}]
