resource Sensitive_password_are_mentionned 'Microsoft.ContainerRegistry/registries/tokens@2022-12-01' = {
  name: 'Sensitive: password are mentionned'
  properties: {
    credentials: {
      certificates: [
        {
          '...': 'certificate details'
        }
      ]
      passwords: [ // Noncompliant{{This authentication method is not certificate-based. Make sure it is safe here.}}
        {
          '...': 'password details'
        }
      ]
    }
  }
}

resource Sensitive_certificates_is_empty 'Microsoft.ContainerRegistry/registries/tokens@2022-12-01' = {
  name: 'Sensitive: certificates is empty'
  properties: {
    credentials: {
      certificates: [] // Noncompliant{{Omitting a list of certificates disables certificate-based authentication. Make sure it is safe here.}}
      passwords: []
    }
  }
}

resource Sensitive_certificates_property_is_missing 'Microsoft.ContainerRegistry/registries/tokens@2022-12-01' = {
  name: 'Sensitive: certificates property is missing'
  properties: {
    credentials: { // Noncompliant{{Omitting "certificates" disables certificate-based authentication. Make sure it is safe here.}}
      passwords: []
    }
  }
}

resource Sensitive_both_certificates_and_password_property_are_missing 'Microsoft.ContainerRegistry/registries/tokens@2022-12-01' = {
  name: 'Sensitive: both certificates and password property are missing'
  properties: {
    credentials: {} // Noncompliant
//               ^^
  }
}

resource Sesitive_nested_resource 'Microsoft.ContainerRegistry/registries@2022-12-01' = {
  name: 'Sesitive nested resource'

  resource Sesitive_nested_resource_nested_resource 'tokens@2022-12-01' = {
    name: 'nested resource'
    properties: {
      credentials: {
        certificates: [
          {
            '...': 'certificate details'
          }
        ]
        passwords: [ // Noncompliant
          {
            '...': 'password details'
          }
        ]
      }
    }
  }
}

resource Compliant_certificates_are_present_and_passwords_are_empty 'Microsoft.ContainerRegistry/registries/tokens@2022-12-01' = {
  name: 'Compliant: certificates are present and passwords are empty'
  properties: {
    credentials: {
      certificates: [
        {
          '...': 'certificate details'
        }
      ]
      passwords: []
    }
  }
}

resource Compliant_certificates_are_present_and_no_passwords_property 'Microsoft.ContainerRegistry/registries/tokens@2022-12-01' = {
  name: 'Compliant: certificates are present and no passwords property'
  properties: {
    credentials: {
      certificates: [
        {
          '...': 'certificate details'
        }
      ]
    }
  }
}

resource Compliant_nested_resource 'Microsoft.ContainerRegistry/registries@2022-12-01' = {
  name: 'Compliant nested resource'

  resource Compliant_nested_resource_nested_resource 'Microsoft.ContainerRegistry/registries/tokens@2022-12-01' = {
    name: 'nested resource'
    properties: {
      credentials: {
        certificates: [
          {
            '...': 'certificate details'
          }
        ]
        passwords: []
      }
    }
  }
}

resource Extra_use_case_certificates_is_not_an_array 'Microsoft.ContainerRegistry/registries/tokens@2022-12-01' = {
  name: 'Extra use case: certificates is not an array'
  properties: {
    credentials: {
      certificates: {
        '...': 'certificate details'
      }
    }
  }
}

resource Extra_use_case_no_credentials_properties 'Microsoft.ContainerRegistry/registries/tokens@2022-12-01' = {
  name: 'Extra use case: no credentials properties'
  properties: {}
}

resource Extra_use_case_passwords_is_not_an_array 'Microsoft.ContainerRegistry/registries/tokens@2022-12-01' = {
  name: 'Extra use case: passwords is not an array'
  properties: {
    credentials: {
      certificates: [
        {
          '...': 'certificate details'
        }
      ]
      passwords: {
        '...': 'password details'
      }
    }
  }
}
