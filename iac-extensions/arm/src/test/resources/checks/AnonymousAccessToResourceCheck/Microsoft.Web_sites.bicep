// Noncompliant@+1 {{Omitting authsettingsV2 disables authentication. Make sure it is safe here.}}
resource appService 'Microsoft.Web/sites@2022-09-01' = {
    name: 'example'
}

resource appService 'Microsoft.Web/sites/config@2022-09-01' = {
    name: 'authsettingsV2'
    properties: {
        globalValidation: {
            requireAuthentication: true
            // Noncompliant@+1 {{Make sure that disabling authentication is safe here.}}
            unauthenticatedClientAction: 'AllowAnonymous'
        }
    }
}

resource appService 'Microsoft.Web/sites@2022-09-01' = {
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
