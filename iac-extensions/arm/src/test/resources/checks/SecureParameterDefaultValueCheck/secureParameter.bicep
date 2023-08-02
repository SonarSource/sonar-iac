param normalString string
@secure()
param secureString string

// Sensitive use cases
@secure()
param nonCompliant1 string = 'my secret' // Noncompliant{{Remove the default value from this secure string.}}
//                           ^^^^^^^^^^^
@secure()
param nonCompliant2 object = {key:'my secret'} // Noncompliant{{Remove the default value from this secure object.}}
//                           ^^^^^^^^^^^^^^^^^
@secure()
param nonCompliant3 string = '[newGuid()]' // Noncompliant
@secure()
param nonCompliant4 string = uniqueString('secret') // Noncompliant
@secure()
param nonCompliant5 string = normalString // Noncompliant
@secure()
param nonCompliant6 string = unknownParam // Noncompliant

// Compliant use cases
@secure()
param compliant1 string
@secure()
param compliant2 string = ''
@secure()
param compliant3 object
@secure()
param compliant4 object = {}
@secure()
param compliant5 string = newGuid()
param compliant6 string = 'secret'
@secure()
param compliant7 unknown = 'secret'
@secure()
param compliant8 string = secureString
