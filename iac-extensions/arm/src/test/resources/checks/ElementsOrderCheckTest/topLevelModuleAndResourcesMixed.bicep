targetScope = 'subscription'
metadata myMetadata = 123
param myParam int
var myVar = 'text'
resource myExistingRes1 'type@version' existing = {}
module myMod1 './myMod.bicep' = {}

// Noncompliant@+1
   resource myRes1 'type@version' = {}
// ^^^^^^^^

module myMod2 './myMod.bicep' = {}
resource myRes2 'type@version' = {}
output myOutput1 string = myVar
