targetScope = 'subscription'
param myParam int
var myVar = 'text'
resource myRes1 'type@version' = {}
module myMod './myMod.bicep' = {}
output myOutput string = myVar
