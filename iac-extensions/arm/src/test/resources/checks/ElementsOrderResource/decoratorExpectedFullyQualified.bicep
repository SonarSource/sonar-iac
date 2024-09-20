@sys.description('some description')
@batchSize(5)
resource resourceName 'type@version' = {
}

// Compliant, because it's invalid syntax for decorators; the rule does nothing
@foo::sys.description('some description')
@foo.sys().batchSize(5)
@foo.bar!.baz()
resource resourceName1 'type@version' = {
}
