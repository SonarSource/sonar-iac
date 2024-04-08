// Noncompliant@+1 {{Rename this parameter "storage_account_name" to match the regular expression '^[a-z][a-zA-Z0-9]*$'.}}
param storage_account_name string
//    ^^^^^^^^^^^^^^^^^^^^

param StorageAccountName string     // Noncompliant

param storageAccountName string     // Compliant

// Noncompliant@+1  {{Rename this variable "string_variable" to match the regular expression '^[a-z][a-zA-Z0-9]*$'.}}
var string_variable = 'example val'
//  ^^^^^^^^^^^^^^^

var StringVariable = 'example val'   // Noncompliant

var stringVariable = 'example val'   // Compliant

resource keyvault 'Microsoft.KeyVault/vaults@2019-09-01' = {
  name: storage_account_name
  properties: {
    param2: StorageAccountName
    param3: storageAccountName
    var1: string_variable
    var2: StringVariable
    var3: stringVariable
  }
}
