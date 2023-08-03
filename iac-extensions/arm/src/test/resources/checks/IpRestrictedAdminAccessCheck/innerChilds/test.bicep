resource parent_resource_case_1_parent_parent_child 'Microsoft.Network/networkSecurityGroups@2022-11-01' = {
  name: 'Parent resource case 1 : parent/parent -> child'

  // Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
  resource parent_resource_case_1_parent_parent_child_inner_child 'securityRules@2022-11-01' = {
  //                                                              ^^^^^^^^^^^^^
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

resource parent_resource_case_2_parent_parent_2_child 'Microsoft.Network@2022-11-01' = {
  name: 'Parent resource case 2 : parent -> parent 2 -> child'

  resource parent_resource_case_2_parent_parent_2_child_parent_2 'networkSecurityGroups@2022-11-01' = {
    name: 'Parent resource case 2 : parent -> parent 2 -> child/parent 2'

    // Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
    resource parent_resource_case_2_parent_parent_2_child_parent_2_child 'securityRules@2022-11-01' = {
    //                                                                   ^^^^^^^^^^^^^
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

resource parent_resource_case_3_parent_child_child 'Microsoft.Network@2022-11-01' = {
  name: 'Parent resource case 3 : parent -> child/child'


  // Noncompliant@+1 {{Restrict IP addresses authorized to access administration services.}}
  resource parent_resource_case_3_parent_child_child_child_child 'networkSecurityGroups/securityRules@2022-11-01' = {
  //                                                             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
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
