// Noncompliant@+1 {{Rename this parameter "storage_account_name" to match the regular expression '^[a-z][a-zA-Z0-9]*$'.}}
param storage_account_name string
//    ^^^^^^^^^^^^^^^^^^^^

param StorageAccountName string     // Noncompliant

param storageAccountName string     // Compliant

param demo_Int int         // Noncompliant

param demoInt int          // Compliant

param _demo_bool bool      // Noncompliant

param demoBool bool        // Compliant

param demo_Object object   // Noncompliant

param demoObject object    // Compliant

param DemoArray array      // Noncompliant

param demoArray array      // Compliant

@secure()
param AdminPassword string // Noncompliant

@secure()
param password string      // Compliant

// --- parameters ------------------------------

// Noncompliant@+1  {{Rename this variable "string_variable" to match the regular expression '^[a-z][a-zA-Z0-9]*$'.}}
var string_variable = 'example val'
//  ^^^^^^^^^^^^^^^

var StringVariable = 'example val'   // Noncompliant

var stringVariable = 'example val'   // Compliant

param item_count int = 3             // Noncompliant
param itemCount int = 3              // Compliant

param ExampleBool bool = true        // Noncompliant
param exampleBool bool = true        // Compliant

param example_object object = {name: 'test name'}  // Noncompliant
param exampleObject object = {name: 'test name'}   // Compliant

var IntegerArray = [1 2 3]  // Noncompliant
var integerArray = [1 2 3]  // Compliant

// --- usages of parameters and variables

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
