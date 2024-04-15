resource resourceName 'type@version' = {
  properties: {}
// Noncompliant@+1
  parent: parentRef
//^^^^^^
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
}
