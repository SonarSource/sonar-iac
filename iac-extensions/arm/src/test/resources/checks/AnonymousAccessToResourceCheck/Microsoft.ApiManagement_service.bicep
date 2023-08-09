// Noncompliant@+1 {{Omitting sign_in authorizes anonymous access. Make sure it is safe here.}}
resource apiService 'Microsoft.ApiManagement/service@2022-09-01-preview' = {
//                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  name: 'apiService'
}

resource apiService 'Microsoft.ApiManagement/service@2022-09-01-preview' = {
  name: 'apiService'
  resource portalSettings 'portalsettings@2022-09-01-preview' = {
    name: 'signin'
    properties: {
      // Noncompliant@+1 {{Make sure that giving anonymous access without enforcing sign-in is safe here.}}
      enabled: false
    }
  }
}

// Noncompliant@+1 {{Omitting sign_in authorizes anonymous access. Make sure it is safe here.}}
resource apiService 'Microsoft.ApiManagement/service@2022-09-01-preview' = {
  name: 'apiService'
  // Noncompliant@+1 {{Omitting authenticationSettings disables authentication. Make sure it is safe here.}}
  resource portalSettings 'apis@2022-09-01-preview' = {
    name: 'exampleApi'
  }
}

// Noncompliant@+1 {{Omitting authenticationSettings disables authentication. Make sure it is safe here.}}
resource portalSettings 'Microsoft.ApiManagement/service/apis@2022-09-01-preview' = {
  name: 'exampleApi'
  parent: apiService
}

resource apiService 'Microsoft.ApiManagement/service@2022-09-01-preview' = {
  name: 'apiService'
  resource portalSettings 'portalsettings@2022-09-01-preview' = {
    name: 'signin'
    properties: {
      enabled: true
    }
  }
  resource apis 'apis@2022-09-01-preview' = {
    name: 'exampleApi'
    properties: {
      authenticationSettings: {
        openid: {
          bearerTokenSendingMethods: ['authorizationHeader']
          openidProviderId: '<an OpenID provider ID>'
        }
      }
    }
  }
}

// false positive due to child resource not being recognized - will be fixed by https://sonarsource.atlassian.net/browse/SONARIAC-1044
// Noncompliant@+1 {{Omitting sign_in authorizes anonymous access. Make sure it is safe here.}}
resource apiServiceParent 'Microsoft.ApiManagement/service@2022-09-01-preview' = {
  name: 'apiService'
}
resource portalSettings 'portalsettings@2022-09-01-preview' = {
  name: 'signin'
  parent: apiServiceParent
  properties: {
    enabled: true
  }
}
resource apis 'apis@2022-09-01-preview' = {
  name: 'exampleApi'
  parent: apiServiceParent
  properties: {
    authenticationSettings: {
      openid: {
        bearerTokenSendingMethods: ['authorizationHeader']
        openidProviderId: '<an OpenID provider ID>'
      }
    }
  }
}

resource portalSettings 'Microsoft.ApiManagement/service/portalsettings@2022-09-01-preview' = {
  name: 'incorrect_resource_name'  // Incorrect resource name - won't be checked
  parent: apiServiceParent
  properties: {
    enabled: false
  }
}

resource portalSettings 'Microsoft.ApiManagement/service/portalsettings@2022-09-01-preview' = {
  name: 'signIn'
  parent: apiServiceParent
  properties: {
    // Noncompliant@+1
    enabled: false
  }
}
