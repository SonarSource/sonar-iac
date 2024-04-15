resource resourceName1 'type@version' = {
  parent: parentRef
  scope: demo
  name: 'Compliant expected order'
  location: location
}

resource resourceName2 'type@version' = {
  parent: parentRef
  scope: demo
  name: 'Compliant expected order'
  location: location
  kind: {}
// Noncompliant@+1
  sku: {}
//^^^
  plan: {}
  identity: {}
  dependsOn: {}
  tags: {}
  otherProperty: {}
  properties: {}
}

resource resourceName3 'type@version' = {
  parent: parentRef
  scope: demo
  name: 'Compliant expected order'
  location: location
  zones: {}
  sku: {}
  kind: {}
  scale: {}
  plan: {}
  identity: {}
  dependsOn: {}
  tags: {}
  otherProperty: {}
  properties: {}
}
