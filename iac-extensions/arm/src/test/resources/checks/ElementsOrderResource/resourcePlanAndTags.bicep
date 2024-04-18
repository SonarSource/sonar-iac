resource resourceName 'type@version' = {
  parent: parentRef
  scope: demo
  name: 'Compliant expected order'
  location: location
  zones: {}
  sku: {}
  kind: {}
  scale: {}
  tags: {}
// Noncompliant@+1
  identity: {}
//^^^^^^^^
  dependsOn: {}
  plan: {}
  otherProperty: {}
  properties: {}
}
