resource resourceName 'type@version' = {
  scope: demo
// Noncompliant@+1
  parent: parentRef
//^^^^^^
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
