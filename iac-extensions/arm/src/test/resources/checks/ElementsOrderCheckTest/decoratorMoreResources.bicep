@batchSize(5)
// Noncompliant@+1
  @description('some description')
//^^^^^^^^^^^^
resource resourceName 'type@version' = {
}

@description('some description')
@batchSize(5)
resource resourceName 'type@version' = {
}

@customDecorator('custom')
// Noncompliant@+1
  @description('some description')
//^^^^^^^^^^^^
@batchSize(5)
resource resourceName 'type@version' = {
}
