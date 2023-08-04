resource noncompliant1 'Microsoft.Scheduler/jobCollections/jobs@2016-01-01' = {
  name: 'Sensitive: no certificate authentication on action.request.authentication.type'
  properties: {
    action: {
      request: {
        authentication: {
          type: 'Basic' // Noncompliant{{This authentication method is not certificate-based. Make sure it is safe here.}}
//        ^^^^^^^^^^^^^
        }
      }
    }
  }
}

resource noncompliant2 'Microsoft.Scheduler/jobCollections/jobs@2016-01-01' = {
  name: 'Sensitive: no certificate authentication on action.errorAction.request.authentication.type'
  properties: {
    action: {
      errorAction: {
        request: {
          authentication: {
            type: 'Basic' // Noncompliant
          }
        }
      }
    }
  }
}

resource noncompliant3 'Microsoft.Scheduler/jobCollections@2016-01-01' = {
  name: 'Sensitive: nested child resource with sensitive configuration'

  resource noncompliant3_nested_child 'Microsoft.Scheduler/jobCollections/jobs@2016-01-01' = {
    name: 'nested child'
    properties: {
      action: {
        errorAction: {
          request: {
            authentication: {
              type: 'Basic' // Noncompliant
            }
          }
        }
      }
    }
  }
}

resource compliant1 'Microsoft.Scheduler/jobCollections/jobs@2016-01-01' = {
  name: 'Compliant'
  properties: {
    action: {
      request: {
        authentication: {
          type: 'ClientCertificate'
        }
      }
    }
  }
}

resource compliant2 'Microsoft.Scheduler/jobCollections/jobs@2016-01-01' = {
  name: 'Compliant'
  properties: {
    action: {
      errorAction: {
        request: {
          authentication: {
            type: 'ClientCertificate'
          }
        }
      }
    }
  }
}
