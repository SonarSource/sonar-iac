resource sensitiveApiManagementService1 '${type}@2021-08-01' = {
  name: 'Sensitive: no identity.type is defined'
  location: location
}

resource sensitiveApiManagementService2 '${type}@2021-08-01' = {
  name: 'Compliant: identity.type is defined and it is not None'
  location: location
  identity: {
    type: 'SystemAssigned'
  }
}

resource sensitiveApiManagementService3 '${type}@2021-08-01' = {
  name: 'Sensitive: identity.type is defined'
  location: location
  identity: {
    type: 'None'
  }
}

resource sensitiveApiManagementService3 '${type}@2021-08-01' = {
  name: 'Compliant: identity.type is not string literal'
  location: location
  identity: {
    type: {}
  }
}

resource sensitiveApiManagementService3 '${type}@2021-08-01' = {
  name: 'Sensitive: identity is defined but identity.type not'
  location: location
  identity: {
    foo: 'bar'
  }
}
