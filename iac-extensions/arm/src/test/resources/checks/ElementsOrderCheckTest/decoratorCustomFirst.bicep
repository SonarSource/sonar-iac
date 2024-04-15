@customDecorator()
// Noncompliant@+1
  @description('some description')
//^^^^^^^^^^^^
@batchSize(5)
resource resourceName 'type@version' = {
}
