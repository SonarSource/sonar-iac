resource noncompliant1 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is false'
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: false // Noncompliant{{Enable client certificate authentication for this resource.}}
  }
}

resource noncompliant2 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Sensitive: clientCertMode is not \'Required\''
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertMode: 'Optional' // Noncompliant{{Require client certificates for this resource.}}
  }
}

resource noncompliant3 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is true but clientCertMode is not \'Required\''
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: true
    clientCertMode: 'Optional' // Noncompliant
  }
}

resource noncompliant4 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Sensitive: clientCertEnabled is false but clientCertMode is \'Required\''
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: false // Noncompliant
    clientCertMode: 'Required'
  }
}

resource noncompliant5 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive x2: parent resource is compliant but nested child resource override with unsafe values'
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: true
    clientCertMode: 'Required'
  }

  resource noncompliant5_nested_child_1 'slots@2015-08-01' = {
    name: 'Nested child 1'
    properties: {
      publicNetworkAccess: 'Disabled'
      clientCertEnabled: false // Noncompliant
    }
  }

  resource noncompliant5_nested_child_2 'slots@2015-08-01' = {
    name: 'Nested child 2'
    properties: {
      publicNetworkAccess: 'Disabled'
      clientCertMode: 'Optional' // Noncompliant
    }
  }
}

resource noncompliant6 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Sensitive: parent is sensitive, even if child override with compliant value it\'s still sensitive'
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: false // Noncompliant
    clientCertMode: 'Required'
  }

  resource noncompliant6_nested_child 'Microsoft.Web/sites/slots@2015-08-01' = {
    name: 'Nested child'
    properties: {
      publicNetworkAccess: 'Disabled'
      clientCertEnabled: true
    }
  }
}

resource compliant1 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Compliant: clientCertEnabled is missing and clientCertMode is defined and is \'Required\''
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertMode: 'Required'
  }
}

resource compliant2 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Compliant: clientCertEnabled is true and clientCertMode is missing'
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: true
  }
}

resource compliant3 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Compliant: clientCertEnabled is true and clientCertMode is \'Required\''
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: true
    clientCertMode: 'Required'
  }
}

resource compliant4 'Microsoft.Web/sites@2015-08-01' = {
  name: 'Compliant: parent is compliant and child override with compliant value'
  properties: {
    publicNetworkAccess: 'Disabled'
    clientCertEnabled: true
    clientCertMode: 'Required'
  }

  resource compliant4_nested_child 'Microsoft.Web/sites/slots@2015-08-01' = {
    name: 'Nested child'
    properties: {
      publicNetworkAccess: 'Disabled'
      clientCertEnabled: true
    }
  }
}

resource compliant5 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Compliant: both clientCertEnabled and clientCertMode are missing'
  properties: {
    publicNetworkAccess: 'Disabled'
  }
}

resource compliant6 'another type@2015-08-01' = {
  name: 'Compliant: the resource type is not in the scope of the rule'
  properties: {
    clientCertEnabled: false
  }
}

resource compliant_public 'Microsoft.Web/sites/slots@2015-08-01' = {
  name: 'Compliant: publicNetworkAccess not Disabled'
  properties: {
    clientCertEnabled: false
    clientCertMode: 'Optional'
  }
}
