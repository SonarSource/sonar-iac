resource linkedService 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
    name: 'example'
    properties: {
        type: 'Web'
        typeProperties: {
            // Noncompliant@+1 {{Make sure that authorizing anonymous access is safe here.}}
            authenticationType: 'Anonymous'
        }
    }
}

@secure()
@description('The password for authentication')
param password string

resource linkedService 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
    name: 'example'
    properties: {
        type: 'Web'
        typeProperties: {
            authenticationType: 'Basic' // Compliant
            username: 'test'
            password: {
                type: 'SecureString'
                value: password
            }
        }
    }
}


resource linkedService 'Microsoft.DataFactory/factories/linkedservices@2018-06-01' = {
    name: 'example'
    properties: {
            // Compliant - not a sensitive type
        type: 'MariaDB'
        typeProperties: {
            authenticationType: 'Anonymous'
        }
    }
}
