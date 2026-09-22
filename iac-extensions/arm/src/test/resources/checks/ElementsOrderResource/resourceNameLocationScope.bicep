resource resourceName 'type@version' = {
  parent: parentRef
  name: 'Noncompliant order'
  location: location
// Noncompliant@+1
  scope: demo
//^^^^^
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
