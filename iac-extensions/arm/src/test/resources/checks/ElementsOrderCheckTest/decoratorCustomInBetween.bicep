@description('some description')
@customDecorator()
// Noncompliant@+1
  @batchSize(5)
//^^^^^^^^^^
resource resourceName 'type@version' = {
}
