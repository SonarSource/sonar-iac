resource noncompliant1 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is false'
  properties: {
    clientCertEnabled: false // Noncompliant{{Make sure that disabling certificate-based authentication is safe here.}}
  }
}

resource noncompliant2 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Sensitive: clientCertMode is not \'Required\''
  properties: {
    clientCertMode: 'Optional' // Noncompliant{{Connections without client certificates will be permitted. Make sure it is safe here.}}
  }
}

resource noncompliant3 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is true but clientCertMode is not \'Required\''
  properties: {
    clientCertEnabled: true
    clientCertMode: 'Optional' // Noncompliant
  }
}

resource noncompliant4 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is false but clientCertMode is \'Required\''
  properties: {
    clientCertEnabled: false // Noncompliant
    clientCertMode: 'Required'
  }
}

resource noncompliant5 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive x2: parent resource is compliant but nested child resource override with unsafe values'
  properties: {
    clientCertEnabled: true
    clientCertMode: 'Required'
  }

  resource noncompliant5_nested_child_1 'slots@2015-08-01' = {
    name: 'Nested child 1'
    properties: {
      clientCertEnabled: false // Noncompliant
    }
  }

  resource noncompliant5_nested_child_2 'slots@2015-08-01' = {
    name: 'Nested child 2'
    properties: {
      clientCertMode: 'Optional' // Noncompliant
    }
  }
}

resource noncompliant6 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: parent is sensitive, even if child override with compliant value it\'s still sensitive'
  properties: {
    clientCertEnabled: false // Noncompliant
    clientCertMode: 'Required'
  }

  resource noncompliant6_nested_child 'Microsoft.Web/sites/slots@2015-08-01' = {
    name: 'Nested child'
    properties: {
      clientCertEnabled: true
    }
  }
}

resource compliant1 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Compliant: clientCertEnabled is missing and clientCertMode is defined and is \'Required\''
  properties: {
    clientCertMode: 'Required'
  }
}

resource compliant2 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Compliant: clientCertEnabled is true and clientCertMode is missing'
  properties: {
    clientCertEnabled: true
  }
}

resource compliant3 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Compliant: clientCertEnabled is true and clientCertMode is \'Required\''
  properties: {
    clientCertEnabled: true
    clientCertMode: 'Required'
  }
}

resource compliant4 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Compliant: parent is compliant and child override with compliant value'
  properties: {
    clientCertEnabled: true
    clientCertMode: 'Required'
  }

  resource compliant4_nested_child 'Microsoft.Web/sites/slots@2015-08-01' = {
    name: 'Nested child'
    properties: {
      clientCertEnabled: true
    }
  }
}

resource compliant5 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Compliant: both clientCertEnabled and clientCertMode are missing'
  properties: {}
}

resource compliant6 'another type@2015-08-01' = {
  name: 'Compliant: the resource type is not in the scope of the rule'
  properties: {
    clientCertEnabled: false
  }
}
