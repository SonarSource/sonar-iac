metadata myMetadata = 123
param myParam int
var myVar = 'text'
resource myExistingRes1 'type@version' existing = {}
resource myRes1 'type@version' = {}
module myMod './myMod.bicep' = {}
output myOutput string = myVar

// Noncompliant@+1
   targetScope = 'subscription'
// ^^^^^^^^^^^

