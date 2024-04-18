@customDecorator()
// Noncompliant@+1 {{Reorder the decorators to match the recommended order.}}
  @description('some description')
//^^^^^^^^^^^^
@batchSize(5)
resource resourceName 'type@version' = {
}
