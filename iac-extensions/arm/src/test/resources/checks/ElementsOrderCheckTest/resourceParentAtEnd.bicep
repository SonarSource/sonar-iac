resource resourceName 'type@version' = {
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

  // Noncompliant@+1
  parent: parentRef
//^^^^^^
}
