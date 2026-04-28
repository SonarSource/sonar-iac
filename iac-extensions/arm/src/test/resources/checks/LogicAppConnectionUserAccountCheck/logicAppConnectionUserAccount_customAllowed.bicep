// With allowedConnectors = "sql", the sql connector becomes exempt
resource sqlConnection 'Microsoft.Web/connections@2016-06-01' = {
  name: 'sql-connection-now-allowed'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'sql')
    }
    parameterValues: {
      username: 'john.doe@company.com'
      password: sqlPassword
    }
  }
}

// With allowedConnectors = "sql", the office365 connector is no longer exempt
resource office365Connection 'Microsoft.Web/connections@2016-06-01' = {
  name: 'office365-now-flagged'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'office365')
    }
    parameterValues: {
      authenticatedUser: 'jane.smith@company.com' // Noncompliant
    }
  }
}
