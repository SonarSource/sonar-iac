targetScope = 'subscription'
metadata myMetadata = 123
param myParam int
var myVar = 'text'
resource myRes1 'type@version' = {}

// Noncompliant@+1
   resource myExistingRes1 'type@version' existing = {}
// ^^^^^^^^

resource myRes1 'type@version' = {}
module myMod './myMod.bicep' = {}
output myOutput string = myVar
