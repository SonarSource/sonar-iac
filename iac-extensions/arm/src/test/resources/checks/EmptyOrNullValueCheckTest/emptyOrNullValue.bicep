resource exampleStorage 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: 'my name'
  properties: { // Noncompliant
//^[el=+2;ec=3]
  }
}

resource exampleStorage 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: null              // Noncompliant {{Remove this null property or complete with real code.}}
//^^^^^^^^^^
  kind: ''                // Noncompliant {{Remove this empty string or complete with real code.}}
//^^^^^^^^
  tags: {}                // Noncompliant {{Remove this empty object or complete with real code.}}
//^^^^^^^^
  dependsOn: []           // Noncompliant {{Remove this empty array or complete with real code.}}
//^^^^^^^^^^^^^
  properties: {
    prop1: null           // Noncompliant {{Remove this null property or complete with real code.}}
    prop2: ''             // Noncompliant {{Remove this empty string or complete with real code.}}
    prop3: {}             // Noncompliant {{Remove this empty object or complete with real code.}}
    prop4: []             // Noncompliant {{Remove this empty array or complete with real code.}}
    prop5: 'my string'
    prop6: identifier
    prop7: true
    prop8: {key:'val'}
    prop9: ['val']
    prop10: [
      null,      // Don't report an issue on empty/null value, only if it's the value of a property
      '',
      [],
      {},
      {
        key: null // Noncompliant {{Remove this null property or complete with real code.}}
      }
    ]
  }
  other: [
    {
      key:null    // Noncompliant {{Remove this null property or complete with real code.}}
    }
  ]
}

var var1 = null           // Noncompliant {{Remove this null variable or complete with real code.}}
var var2 = ''             // Noncompliant {{Remove this empty string or complete with real code.}}
var var3 = {}             // Noncompliant {{Remove this empty object or complete with real code.}}
var var4 = []             // Noncompliant {{Remove this empty array or complete with real code.}}

var var5 = 'my string'
var var6 = identifier
var var7 = true
var var8 = {key:'val'}
var var9 = ['val']
var var10 = [
  {
    key: null             // Noncompliant {{Remove this null property or complete with real code.}}
  }
]

param par1 object = null
param par2 string = ''
param par3 object = {}
param par4 array = []

output out1 object = null // Noncompliant {{Remove this null output or complete with real code.}}
output out2 string = ''   // Noncompliant {{Remove this empty string or complete with real code.}}
output out3 object = {}   // Noncompliant {{Remove this empty object or complete with real code.}}
output out4 array = []    // Noncompliant {{Remove this empty array or complete with real code.}}

output out5 string = 'my string'
output out6 string = identifier
output out7 bool = true
output out8 object = {key:'val'}
output out9 array = ['val']
output out10 object = {
  key:null                // Noncompliant {{Remove this null property or complete with real code.}}
}
output out11 array = [
  null,
  '',
  [],
  {},
  {
    key:null              // Noncompliant {{Remove this null property or complete with real code.}}
  }
]

output outIf object = 1 > 2 ? null : '' // Not reporting any issue in case of conditional output

param myList array = [
  'val1'
  'val2'
  'val3'
]
output outFor array = [for (name, i) in myList: {
  name: name
  other1: null  // Noncompliant {{Remove this null output or complete with real code.}}
  other2: ''    // Noncompliant {{Remove this empty string or complete with real code.}}
  other3: {}    // Noncompliant {{Remove this empty object or complete with real code.}}
  other4: []    // Noncompliant {{Remove this empty array or complete with real code.}}
  other5: 'my string'
  other6: identifier
  other7: true
  other8: {key:'val'}
  other9: ['val']
  other10: {
    key:null    // Noncompliant {{Remove this null property or complete with real code.}}
  }
  other11: [
    null,
    '',
    [],
    {},
    {
      key:null  // Noncompliant {{Remove this null property or complete with real code.}}
    }
  ]
}]

output outForOther array = [for (name, i) in myList: []] // Corner case where the for body is not an object
