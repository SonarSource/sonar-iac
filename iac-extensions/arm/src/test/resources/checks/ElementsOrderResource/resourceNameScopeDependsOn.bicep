resource resourceWithLaterViolation 'type@version' = {
  name: 'Valid name before scope'
  scope: demo
  properties: {}
// Noncompliant@+1
  dependsOn: []
//^^^^^^^^^
}
