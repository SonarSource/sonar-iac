resource parentResourceCase1ParentParentChild 'Microsoft.Network/networkSecurityGroups@2022-11-01' = {
  name: 'Parent resource case 1 : parent/parent -> child'

  // Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
  resource parentResourceCase1ParentParentChildInnerChild 'securityRules@2022-11-01' = {
  //                                                      ^^^^^^^^^^^^^
    name: 'inner child'
    properties: {
      direction: 'Inbound'
      //         ^^^^^^^^^< {{Sensitive direction}}
      access: 'Allow'
      //      ^^^^^^^< {{Sensitive access}}
      protocol: 'TCP'
      //        ^^^^^< {{Sensitive protocol}}
      destinationPortRange: '*'
      //                    ^^^< {{Sensitive destination port range}}
      sourceAddressPrefix: '*'
      //                   ^^^< {{Sensitive source address prefix}}
    }
  }
}

resource parentResourceCase2ParentParent2Child 'Microsoft.Network@2022-11-01' = {
  name: 'Parent resource case 2 : parent -> parent 2 -> child'

  resource parentResourceCase2ParentParent2ChildParent2 'networkSecurityGroups@2022-11-01' = {
    name: 'Parent resource case 2 : parent -> parent 2 -> child/parent 2'

    // Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
    resource parentResourceCase2ParentParent2ChildParent2Child 'securityRules@2022-11-01' = {
    //                                                         ^^^^^^^^^^^^^
      name: 'child'
      properties: {
        direction: 'Inbound'
        //         ^^^^^^^^^< {{Sensitive direction}}
        access: 'Allow'
        //      ^^^^^^^< {{Sensitive access}}
        protocol: 'TCP'
        //        ^^^^^< {{Sensitive protocol}}
        destinationPortRange: '*'
        //                    ^^^< {{Sensitive destination port range}}
        sourceAddressPrefix: '*'
        //                   ^^^< {{Sensitive source address prefix}}
      }
    }
  }
}

resource parentResourceCase3ParentChildChild 'Microsoft.Network@2022-11-01' = {
  name: 'Parent resource case 3 : parent -> child/child'


  // Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
  resource parentResourceCase3ParentChildChildChildChild 'networkSecurityGroups/securityRules@2022-11-01' = {
  //                                                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    name: 'Parent resource case 3 : parent -> child/child/child/child'
    properties: {
      direction: 'Inbound'
      //         ^^^^^^^^^< {{Sensitive direction}}
      access: 'Allow'
      //      ^^^^^^^< {{Sensitive access}}
      protocol: 'TCP'
      //        ^^^^^< {{Sensitive protocol}}
      destinationPortRange: '*'
      //                    ^^^< {{Sensitive destination port range}}
      sourceAddressPrefix: '*'
      //                   ^^^< {{Sensitive source address prefix}}
    }
  }
}
