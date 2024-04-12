targetScope = 'subscription'

metadata myMetadata1 = 123
metadata myMetadata2 = 123
metadata myMetadata3 = 123

param myParam1 int
param myParam2 int
param myParam3 int

var myVar1 = 'text'
var myVar2 = 'text'
var myVar3 = 'text'

resource myExistingRes1 'type@version' existing = {}
resource myExistingRes2 'type@version' existing = {}
resource myExistingRes3 'type@version' existing = {}

resource myRes1 'type@version' = {}
resource myRes2 'type@version' = {}
resource myRes3 'type@version' = {}

module myMod1 './myMod.bicep' = {}
module myMod2 './myMod.bicep' = {}
module myMod3 './myMod.bicep' = {}

output myOutput1 string = myVar
output myOutput2 string = myVar
output myOutput3 string = myVar
