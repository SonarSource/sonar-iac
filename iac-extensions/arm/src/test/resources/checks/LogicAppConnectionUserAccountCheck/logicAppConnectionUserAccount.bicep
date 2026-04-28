// SQL connection using a corporate user account
resource sqlConnection 'Microsoft.Web/connections@2016-06-01' = {
  name: 'sql-connection'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'sql')
    }
    parameterValues: {
      server: 'myserver.database.windows.net'
      database: 'mydb'
      username: 'john.doe@company.com' // Noncompliant {{Use a service principal or managed identity instead of a user account for this API connection.}}
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      password: sqlPassword
    }
  }
}

// Service Bus connection with user email
resource servicebusConnection 'Microsoft.Web/connections@2016-06-01' = {
  name: 'servicebus-connection'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'servicebus')
    }
    parameterValues: {
      connectionString: 'Endpoint=sb://mybus.servicebus.windows.net/'
      authenticatedUser: 'jane.smith@company.com' // Noncompliant
    }
  }
}

// Literal api.id path (not an ARM expression)
resource literalIdConnection 'Microsoft.Web/connections@2016-06-01' = {
  name: 'literal-id-connection'
  location: resourceGroup().location
  properties: {
    api: {
      id: '/subscriptions/sub1/providers/Microsoft.Web/locations/eastus/managedApis/sql'
    }
    parameterValues: {
      username: 'admin@corp.example.com' // Noncompliant
    }
  }
}

// Compliant - literal api.id path with allowed connector
resource literalIdOffice365 'Microsoft.Web/connections@2016-06-01' = {
  name: 'literal-id-office365'
  location: resourceGroup().location
  properties: {
    api: {
      id: '/subscriptions/sub1/providers/Microsoft.Web/locations/eastus/managedApis/office365'
    }
    parameterValues: {
      authenticatedUser: 'user@corp.example.com'
    }
  }
}

// Compliant - connection using managed identity
resource sqlConnectionMi 'Microsoft.Web/connections@2016-06-01' = {
  name: 'sql-connection-mi'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'sql')
    }
    parameterValueType: 'Alternative'
    alternativeParameterValues: {}
  }
}

// Compliant - no user email in parameter values
resource servicebusConnectionClean 'Microsoft.Web/connections@2016-06-01' = {
  name: 'servicebus-connection-clean'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'servicebus')
    }
    parameterValues: {
      connectionString: serviceBusConnectionString
    }
  }
}

// Compliant - Office 365 connector (allowed exception)
resource office365Connection 'Microsoft.Web/connections@2016-06-01' = {
  name: 'office365-connection'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'office365')
    }
    parameterValues: {
      'token:clientId': '...'
      authenticatedUser: 'john.doe@company.com'
    }
  }
}

// Compliant - File System connector (allowed exception)
resource filesystemConnection 'Microsoft.Web/connections@2016-06-01' = {
  name: 'filesystem-connection'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'filesystem')
    }
    parameterValues: {
      rootfolder: '\\\\server\\share'
      username: 'admin@company.com'
      password: fileSharePassword
    }
  }
}

// Compliant - literal api.id path with allowed filesystem connector
resource literalIdFilesystem 'Microsoft.Web/connections@2016-06-01' = {
  name: 'literal-id-filesystem'
  location: resourceGroup().location
  properties: {
    api: {
      id: '/subscriptions/sub1/providers/Microsoft.Web/locations/eastus/managedApis/filesystem'
    }
    parameterValues: {
      rootfolder: '\\\\server\\share'
      username: 'admin@corp.example.com'
    }
  }
}

// Compliant - parameterValueType Alternative skips the check, even with email in alternativeParameterValues
resource alternativeWithEmail 'Microsoft.Web/connections@2016-06-01' = {
  name: 'alternative-with-email'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'sql')
    }
    parameterValueType: 'Alternative'
    alternativeParameterValues: {
      authenticatedUser: 'john.doe@company.com'
    }
  }
}

// Compliant - empty parameterValues object
resource emptyParameterValues 'Microsoft.Web/connections@2016-06-01' = {
  name: 'empty-parameter-values'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'sql')
    }
    parameterValues: {}
  }
}

// api.id built from a non-literal expression (concat) referencing an allowed connector → compliant
resource concatAllowedConnector 'Microsoft.Web/connections@2016-06-01' = {
  name: 'concat-allowed'
  location: resourceGroup().location
  properties: {
    api: {
      id: concat('/subscriptions/sub1/providers/Microsoft.Web/locations/eastus/managedApis/', 'office365')
    }
    parameterValues: {
      authenticatedUser: 'user@corp.example.com'
    }
  }
}

// Compliant - email-shaped value lives in a non-credential field (description), not a known user-identifier parameter
resource emailInDescription 'Microsoft.Web/connections@2016-06-01' = {
  name: 'email-in-description'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'sql')
    }
    parameterValues: {
      description: 'owner: jane@x.com'
      server: 'myserver.database.windows.net'
    }
  }
}

// Compliant - sensitive parameter holds a plain identifier (no email shape)
resource sensitiveParamWithoutEmail 'Microsoft.Web/connections@2016-06-01' = {
  name: 'sensitive-param-without-email'
  location: resourceGroup().location
  properties: {
    api: {
      id: subscriptionResourceId('Microsoft.Web/locations/managedApis', resourceGroup().location, 'sql')
    }
    parameterValues: {
      username: 'svc-app-runner'
      password: sqlPassword
    }
  }
}
