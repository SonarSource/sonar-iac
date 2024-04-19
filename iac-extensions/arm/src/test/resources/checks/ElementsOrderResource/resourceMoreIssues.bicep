resource resourceName1 'type@version' = {
  parent: parentRef
  scope: demo
  name: 'Compliant expected order'
  otherProperty: {}
// Noncompliant@+1 {{Reorder the elements to match the recommended order.}}
  location: location
//^^^^^^^^
  properties: {}
}

resource resourceName2 'type@version' = {
  parent: parentRef
  scope: demo
  name: 'Compliant expected order'
  properties: {}
// Noncompliant@+1
  location: location
//^^^^^^^^
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
  tags: {}
// Noncompliant@+1
  dependsOn: {}
//^^^^^^^^^
  properties: {}
}
