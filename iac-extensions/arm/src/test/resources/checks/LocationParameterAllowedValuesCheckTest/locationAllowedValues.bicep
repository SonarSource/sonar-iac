@allowed([ // Noncompliant {{Remove this @allowed decorator from the parameter specifying the location.}}
//^[sc=2;el=+7;ec=2]
  'eastus'
  'westus'
  'northeurope'
  'westeurope'
  'southeastasia'
])
param parameterWithAllowedValues string

@allowed([ // Noncompliant {{Remove this @allowed decorator from the parameter specifying the location.}}
//^[sc=2;el=+3;ec=2]
  'eastus'
])
param parameterWithAllowedValues2 string

// We don't raise here, because it's only used in a string interpolation
// ('${parameterWithAllowedValuesShouldNotBeRaisedBecauseOfUsageInStringInterpolation}concatMe')
// For now we don't traverse the expression tree to check if a parameter is used more deeply
@allowed([
  'eastus'
])
param parameterWithAllowedValuesShouldNotBeRaisedBecauseOfUsageInStringInterpolation string

@allowed([
  'eastus'
])
param parameterWithAllowedValuesShouldNotBeRaisedBecauseOfNoUsage string

@allowed([])
param parameterWithEmptyAllowedValues string

param parameterWithoutAllowedValues string

resource exampleStorage 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: 'foo'
  location: parameterWithAllowedValues
  resource service 'fileServices' = {
    location: parameterWithAllowedValues2
  }
}

resource exampleStorage 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: 'foo'
  location: parameterWithEmptyAllowedValues
}

resource exampleStorage 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: 'foo'
  location: '${parameterWithAllowedValuesShouldNotBeRaisedBecauseOfUsageInStringInterpolation}concatMe'
}

resource exampleStorage 'Microsoft.Storage/storageAccounts@2023-01-01' = {
  name: 'foo'
  location: parameterWithoutAllowedValues
  resource service 'fileServices' = {
    location: parameterWithoutAllowedValues
  }
}

