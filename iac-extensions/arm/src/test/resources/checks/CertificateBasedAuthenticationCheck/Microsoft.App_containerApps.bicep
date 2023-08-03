resource Raise_an_issue_value_is_ignore 'Microsoft.App/containerApps@2022-10-01' = {
  name: 'Raise an issue: value is ignore'
  properties: {
    configuration: {
      ingress: {
        clientCertificateMode: 'ignore' // Noncompliant{{Make sure that disabling certificate-based authentication is safe here.}}
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      }
    }
  }
}

resource Raise_an_issue_value_is_accept 'Microsoft.App/containerApps@2022-10-01' = {
  name: 'Raise an issue: value is accept'
  properties: {
    configuration: {
      ingress: {
        clientCertificateMode: 'accept' // Noncompliant{{Connections without client certificates will be permitted. Make sure it is safe here.}}
      }
    }
  }
}

resource Raise_an_issue_date_1_year_after 'Microsoft.App/containerApps@2023-10-01' = {
  name: 'Raise an issue: date 1 year after'
  properties: {
    configuration: {
      ingress: {
        clientCertificateMode: 'ignore' // Noncompliant
      }
    }
  }
}

resource Raise_an_issue_property_is_missing 'Microsoft.App/containerApps@2022-10-01' = {
  name: 'Raise an issue: property is missing'
  properties: {
    configuration: {
      // Noncompliant@+1{{Omitting "clientCertificateMode" disables certificate-based authentication. Make sure it is safe here.}}
      ingress: {
        another_attr: 'value'
      }
    }
  }
}

resource Compliant_require_value 'Microsoft.App/containerApps@2022-10-01' = {
  name: 'Compliant: require value'
  properties: {
    configuration: {
      ingress: {
        clientCertificateMode: 'require'
      }
    }
  }
}

resource Compliant_date_before_2022_10_01 'Microsoft.App/containerApps@2022-09-01' = {
  name: 'Compliant: date before 2022-10-01'
  properties: {
    configuration: {
      ingress: {
        clientCertificateMode: 'ignore'
      }
    }
  }
}

resource Compliant_unknown_string_value 'Microsoft.App/containerApps@2022-10-01' = {
  name: 'Compliant: unknown string value'
  properties: {
    configuration: {
      ingress: {
        clientCertificateMode: 'unknown string value'
      }
    }
  }
}

resource Compliant_unexpected_expression_type 'Microsoft.App/containerApps@2022-10-01' = {
  name: 'Compliant: unexpected expression type'
  properties: {
    configuration: {
      ingress: {
        clientCertificateMode: 5
      }
    }
  }
}

resource Do_not_raise_an_issue_parent_propery_ingress_is_not_event_present 'Microsoft.App/containerApps@2022-10-01' = {
  name: 'Do not raise an issue: parent propery \'ingress\' is not event present'
  properties: {
    configuration: {}
  }
}
