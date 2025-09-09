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
param nonCompliant3 string = '[newGuid()]' // Noncompliant{{Remove the default value from this secure string.}}
//                           ^^^^^^^^^^^^^
@secure()
param nonCompliant4 string = uniqueString('secret') // Noncompliant
@secure()
param nonCompliant5 string = normalString // Noncompliant
@secure()
param nonCompliant6 string = unknownParam // Noncompliant
@secure()
param nonCompliant7 string = unknownFunction('string') // Noncompliant
@secure()
param nonCompliant8 string = other(1, 2, 3, 'string') // Noncompliant
@secure()
param nonCompliant9 string = 'newGuid()-staticstring' // Noncompliant
@secure()
param nonCompliant10 string = concat('something', '-staticstring') // Noncompliant

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
@secure()
param compliant11 string = uniqueString(newGuid())
@secure()
param compliant12 string = uniqueString(89, null)
@secure()
param compliant14 string = '${newGuid()}'
@secure()
param compliant15 string = '${newGuid()}-staticstring-${newGuid()}'
@secure()
param compliant16 string = concat('${newGuid()}', '-staticstring')
@secure()
param compliant17 string = concat(newGuid(), '-staticstring')
@secure()
param compliant18 string = concat('staticstring-', newGuid())
