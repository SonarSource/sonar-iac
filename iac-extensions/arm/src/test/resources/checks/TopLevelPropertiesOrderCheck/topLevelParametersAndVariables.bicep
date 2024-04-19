targetScope = 'subscription'
metadata myMetadata = 123
var myVar = 'text'

// Noncompliant@+1
   param myParam int
// ^^^^^

resource myExistingRes1 'type@version' existing = {}
resource myRes1 'type@version' = {}
module myMod './myMod.bicep' = {}
output myOutput string = myVar
