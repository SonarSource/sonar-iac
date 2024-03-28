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
param nonCompliant3 string = '[newGuid()]' // False Negative, TODO SONARIAC-1038
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
param compliant3 string = ' '
@secure()
param compliant4 string = null
@secure()
param compliant5 object
@secure()
param compliant6 object = {}
@secure()
param compliant7 string = newGuid()
param compliant8 string = 'secret'
@secure()
param compliant9 unknown = 'secret'
@secure()
param compliant10 string = secureString
