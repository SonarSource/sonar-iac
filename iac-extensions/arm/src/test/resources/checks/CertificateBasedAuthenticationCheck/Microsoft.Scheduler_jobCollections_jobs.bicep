resource Sensitive_no_certificate_authentication_on_action_request_authentication_type 'Microsoft.Scheduler/jobCollections/jobs@2016-01-01' = {
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

resource Sensitive_no_certificate_authentication_on_action_errorAction_request_authentication_type 'Microsoft.Scheduler/jobCollections/jobs@2016-01-01' = {
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

resource Sensitive_nested_child_resource_with_sensitive_configuration 'Microsoft.Scheduler/jobCollections@2016-01-01' = {
  name: 'Sensitive: nested child resource with sensitive configuration'

  resource Sensitive_nested_child_resource_with_sensitive_configuration_nested_child 'Microsoft.Scheduler/jobCollections/jobs@2016-01-01' = {
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

resource Microsoft_Scheduler_jobCollections_jobs_Compliant 'Microsoft.Scheduler/jobCollections/jobs@2016-01-01' = {
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

resource Microsoft_Scheduler_jobCollections_jobs_Compliant 'Microsoft.Scheduler/jobCollections/jobs@2016-01-01' = {
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
