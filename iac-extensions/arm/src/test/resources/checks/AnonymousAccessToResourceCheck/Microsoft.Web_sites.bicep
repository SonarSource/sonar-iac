// Noncompliant@+1 {{Omitting authsettingsV2 disables authentication. Make sure it is safe here.}}
resource appService 'Microsoft.Web/sites@2022-09-01' = {
    name: 'example'
}

// false positive due to child resource not being recognized - will be fixed by https://sonarsource.atlassian.net/browse/SONARIAC-1044
// Noncompliant@+1 {{Omitting authsettingsV2 disables authentication. Make sure it is safe here.}}
resource appService2 'Microsoft.Web/sites@2022-09-01' = {
    name: 'example'
}

resource appServiceConfig 'Microsoft.Web/sites/config@2022-09-01' = {
    name: 'authsettingsV2'
    parent: appService2
    properties: {
        globalValidation: {
            requireAuthentication: true
            // Noncompliant@+1 {{Make sure that disabling authentication is safe here.}}
            unauthenticatedClientAction: 'AllowAnonymous'
        }
    }
}

// Noncompliant@+1 {{Omitting authsettingsV2 disables authentication. Make sure it is safe here.}}
resource appService3 'Microsoft.Web/sites@2022-09-01' = {
    name: 'example'
    resource authSettings 'config@2022-09-01' = {
        // Incorrect name of config resource
        name: 'authsettings'
        properties: {
            globalValidation: {
                requireAuthentication: true
                unauthenticatedClientAction: 'RedirectToLoginPage'
            }
        }
    }
}


resource appService4 'Microsoft.Web/sites@2022-09-01' = {
    name: 'example'
    resource authSettings 'config@2022-09-01' = {
        name: 'authsettingsV2'
        properties: {
            globalValidation: {
                requireAuthentication: true
                unauthenticatedClientAction: 'RedirectToLoginPage'
            }
        }
    }
}
