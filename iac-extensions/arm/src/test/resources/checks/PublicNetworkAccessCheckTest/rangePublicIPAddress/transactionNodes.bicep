resource Sensitive_Should_raise_issue_for_whole_range 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for whole range'
    resource Sensitive_Should_raise_issue_for_whole_range_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for whole range/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '0.0.0.0' // Noncompliant{{Make sure that allowing public IP addresses is safe here.}}
//                        ^^^^^^^^^
          endIpAddress: '255.255.255.255'
//                      ^^^^^^^^^^^^^^^^^< {{and here}}
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_missing_start 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue missing start'
    resource Sensitive_Should_raise_issue_missing_start_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue missing start/dummy'
    properties: {
      firewallRules: [
        {
          endIpAddress: '9.8.7.6' // Noncompliant
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_missing_end 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue missing end'
    resource Sensitive_Should_raise_issue_missing_end_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue missing end/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '241.0.0.0' // Noncompliant
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_IPs_10_0_0_0 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs < 10.0.0.0'
    resource Sensitive_Should_raise_issue_for_IPs_10_0_0_0_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs < 10.0.0.0/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '1.2.3.4' // Noncompliant
          endIpAddress: '9.255.255.255'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_IPs_10_255_255_255_and_100_64_0_0 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 10.255.255.255 and < 100.64.0.0'
    resource Sensitive_Should_raise_issue_for_IPs_10_255_255_255_and_100_64_0_0_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 10.255.255.255 and < 100.64.0.0/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '11.0.0.0' // Noncompliant
          endIpAddress: '100.63.255.255'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_IPs_100_127_255_255_and_169_254_0_0 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 100.127.255.255 and < 169.254.0.0'
  resource Sensitive_Should_raise_issue_for_IPs_100_127_255_255_and_169_254_0_0_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 100.127.255.255 and < 169.254.0.0/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '100.128.0.0' // Noncompliant
          endIpAddress: '169.253.255.255'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_IPs_169_254_255_255_and_172_16_0_0 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 169.254.255.255 and < 172.16.0.0'
  resource Sensitive_Should_raise_issue_for_IPs_169_254_255_255_and_172_16_0_0_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 169.254.255.255 and < 172.16.0.0/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '169.255.0.0' // Noncompliant
          endIpAddress: '172.15.255.255'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_IPs_172_31_255_255_and_192_0_0_0 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 172.31.255.255 and < 192.0.0.0'
  resource Sensitive_Should_raise_issue_for_IPs_172_31_255_255_and_192_0_0_0_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 172.31.255.255 and < 192.0.0.0/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '172.32.0.0' // Noncompliant
          endIpAddress: '191.255.255.255'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_IPs_192_0_0_255_and_192_0_2_0 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 192.0.0.255 and < 192.0.2.0'
  resource Sensitive_Should_raise_issue_for_IPs_192_0_0_255_and_192_0_2_0_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 192.0.0.255 and < 192.0.2.0/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '192.0.1.0' // Noncompliant
          endIpAddress: '192.0.1.255'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_IPs_192_0_2_255_and_192_168_0_0 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 192.0.2.255 and < 192.168.0.0'
  resource Sensitive_Should_raise_issue_for_IPs_192_0_2_255_and_192_168_0_0_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 192.0.2.255 and < 192.168.0.0/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '192.0.3.0' // Noncompliant
          endIpAddress: '192.167.255.255'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_IPs_192_168_255_255_and_198_18_0_0 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 192.168.255.255 and < 198.18.0.0'
  resource Sensitive_Should_raise_issue_for_IPs_192_168_255_255_and_198_18_0_0_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 192.168.255.255 and < 198.18.0.0/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '192.169.0.0' // Noncompliant
          endIpAddress: '192.17.255.255'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_IPs_198_19_255_255_and_198_51_100_0 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 198.19.255.255 and < 198.51.100.0'
  resource Sensitive_Should_raise_issue_for_IPs_198_19_255_255_and_198_51_100_0_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 198.19.255.255 and < 198.51.100.0/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '198.20.0.0' // Noncompliant
          endIpAddress: '198.51.99.255'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_IPs_198_51_100_255_and_203_0_113_0 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 198.51.100.255 and < 203.0.113.0'
  resource Sensitive_Should_raise_issue_for_IPs_198_51_100_255_and_203_0_113_0_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 198.51.100.255 and < 203.0.113.0/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '198.51.101.0' // Noncompliant
          endIpAddress: '203.0.112.255'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_IPs_203_0_113_255_and_240_0_0_0 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 203.0.113.255 and < 240.0.0.0'
  resource Sensitive_Should_raise_issue_for_IPs_203_0_113_255_and_240_0_0_0_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 203.0.113.255 and < 240.0.0.0/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '203.0.114.0' // Noncompliant
          endIpAddress: '239.255.255.255'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_IPs_240_255_255_254_and_255_255_255_255 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 240.255.255.254 and < 255.255.255.255'
  resource Sensitive_Should_raise_issue_for_IPs_240_255_255_254_and_255_255_255_255_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 240.255.255.254 and < 255.255.255.255/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '240.255.255.255' // Noncompliant
          endIpAddress: '255.255.255.255'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_IPs_240_255_255_254_no_endIpAddress 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 240.255.255.254 no endIpAddress'
  resource Sensitive_Should_raise_issue_for_IPs_240_255_255_254_no_endIpAddress_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 240.255.255.254 no endIpAddress/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '240.255.255.255' // Noncompliant
        }
      ]
    }
  }
}

resource Microsoft_Blockchain_blockchainMembers_Sensitive_Should_raise_issue_for_IPs_192_0_0_0_and_192_0_2_255_IP_ranges_edges_for_2_different_groups_between_are_public_addresses 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 192.0.0.0 and < 192.0.2.255 (IP ranges edges for 2 different groups, between are public addresses)'
  resource Microsoft_Blockchain_blockchainMembers_transactionNodes_Sensitive_Should_raise_issue_for_IPs_192_0_0_0_and_192_0_2_255_IP_ranges_edges_for_2_different_groups_between_are_public_addresses_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 192.0.0.0 and < 192.0.2.255 (IP ranges edges for 2 different groups, between are public addresses)/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '192.0.0.0' // Noncompliant
          endIpAddress: '192.0.2.255'
        }
      ]
    }
  }
}

resource Microsoft_Blockchain_blockchainMembers_Sensitive_Should_raise_issue_for_IPs_192_0_0_0_and_192_0_2_255_IP_ranges_edges_for_2_different_groups_between_are_public_addresses 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for IPs > 192.0.0.0 and < 192.0.2.255 (IP ranges edges for 2 different groups, between are public addresses)'
  resource Microsoft_Blockchain_blockchainMembers_transactionNodes_Sensitive_Should_raise_issue_for_IPs_192_0_0_0_and_192_0_2_255_IP_ranges_edges_for_2_different_groups_between_are_public_addresses_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for IPs > 192.0.0.0 and < 192.0.2.255 (IP ranges edges for 2 different groups, between are public addresses)/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '192.0.0.0' // Noncompliant
          endIpAddress: '192.0.2.255'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_unknown_startIpAddress 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for unknown startIpAddress'
  resource Sensitive_Should_raise_issue_for_unknown_startIpAddress_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for unknown startIpAddress/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: 'unknown' // Noncompliant
          endIpAddress: '250.0.0.0'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_unknown_endIpAddress 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for unknown endIpAddress'
  resource Sensitive_Should_raise_issue_for_unknown_endIpAddress_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for unknown endIpAddress/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '250.0.0.0' // Noncompliant
          endIpAddress: 'unknown'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_first_rule 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for first rule'
  resource Sensitive_Should_raise_issue_for_first_rule_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for first rule/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '250.0.0.0' // Noncompliant
          endIpAddress: '251.0.0.0'
        }
        {
          startIpAddress: '240.0.0.0'
          endIpAddress: '240.255.0.0'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_second_rule 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for second rule'
  resource Sensitive_Should_raise_issue_for_second_rule_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for second rule/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '240.0.0.0'
          endIpAddress: '240.255.0.0'
        }
        {
          startIpAddress: '250.0.0.0' // Noncompliant
          endIpAddress: '251.0.0.0'
        }
      ]
    }
  }
}

resource Sensitive_Should_raise_issue_for_both_rules 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Sensitive: Should raise issue for both rules'
  resource Sensitive_Should_raise_issue_for_both_rules_dummy 'transactionNodes@dummy' = {
    name: 'Sensitive: Should raise issue for both rules/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '11.0.0.0' // Noncompliant
          endIpAddress: '11.255.0.0'
        }
        {
          startIpAddress: '250.0.0.0' // Noncompliant
          endIpAddress: '251.0.0.0'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_both_rules_are_compliant 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue, both rules are compliant'
  resource Compliant_Should_NOT_raise_issue_both_rules_are_compliant_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue, both rules are compliant/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '100.64.0.0'
          endIpAddress: '100.127.0.0'
        }
        {
          startIpAddress: '192.0.0.0'
          endIpAddress: '192.0.0.50'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_wrong_subtype 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for wrong subtype'
  resource Compliant_Should_NOT_raise_issue_for_wrong_subtype_dummy 'wrong-subtype@dummy' = {
    name: 'Compliant: Should NOT raise issue for wrong subtype/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '250.0.0.0'
          endIpAddress: 'unknown'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_private_networks_only_1 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for private networks only 1'
  resource Compliant_Should_NOT_raise_issue_for_private_networks_only_1_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for private networks only 1/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '10.0.0.0'
          endIpAddress: '10.255.255.255'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_used_within_an_ISP_s_network_for_carrier_grade_NAT 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for used within an ISP’s network for carrier-grade NAT'
  resource Compliant_Should_NOT_raise_issue_for_used_within_an_ISP_s_network_for_carrier_grade_NAT_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for used within an ISP’s network for carrier-grade NAT/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '100.64.0.0'
          endIpAddress: '100.127.255.255'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_link_local_addresses_for_DHCP_failures 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for link-local addresses for DHCP failures'
  resource Compliant_Should_NOT_raise_issue_for_link_local_addresses_for_DHCP_failures_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for link-local addresses for DHCP failures/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '169.254.0.0'
          endIpAddress: '169.254.255.255'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_private_networks_only_2 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for private networks only 2'
  resource Compliant_Should_NOT_raise_issue_for_private_networks_only_2_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for private networks only 2/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '172.16.0.0'
          endIpAddress: '172.31.255.255'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_IETF_protocol_assignments 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for IETF protocol assignments'
  resource Compliant_Should_NOT_raise_issue_for_reserved_for_IETF_protocol_assignments_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for reserved for IETF protocol assignments/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '192.0.0.0'
          endIpAddress: '192.0.0.255'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_documentation_use_1 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for documentation use 1'
  resource Compliant_Should_NOT_raise_issue_for_reserved_for_documentation_use_1_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for reserved for documentation use 1/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '192.0.2.0'
          endIpAddress: '192.0.2.255'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_private_networks_only_3 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for private networks only 3'
  resource Compliant_Should_NOT_raise_issue_for_private_networks_only_3_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for private networks only 3/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '192.168.0.0'
          endIpAddress: '192.168.255.255'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_inter_subnet_benchmarking 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for inter-subnet benchmarking'
  resource Compliant_Should_NOT_raise_issue_for_reserved_for_inter_subnet_benchmarking_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for reserved for inter-subnet benchmarking/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '198.18.0.0'
          endIpAddress: '198.19.255.255'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_documentation_use_2 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for documentation use 2'
  resource Compliant_Should_NOT_raise_issue_for_reserved_for_documentation_use_2_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for reserved for documentation use 2/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '198.51.100.0'
          endIpAddress: '198.51.100.255'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_documentation_use_3 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for documentation use 3'
  resource Compliant_Should_NOT_raise_issue_for_reserved_for_documentation_use_3_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for reserved for documentation use 3/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '203.0.113.0'
          endIpAddress: '203.0.113.255'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_future_use 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for future use'
  resource Compliant_Should_NOT_raise_issue_for_reserved_for_future_use_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for reserved for future use/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '240.0.0.0'
          endIpAddress: '240.255.255.254'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_future_use_part_of_range 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for future use (part of range)'
  resource Compliant_Should_NOT_raise_issue_for_reserved_for_future_use_part_of_range_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for reserved for future use (part of range)/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '240.1.2.3'
          endIpAddress: '240.100.101.102'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_unknown_value 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for unknown value'
  resource Compliant_Should_NOT_raise_issue_for_unknown_value_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for unknown value/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: 'unknown'
          endIpAddress: 'unknown'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_IP_6 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for IP 6'
  resource Compliant_Should_NOT_raise_issue_for_IP_6_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for IP 6/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: '2001:0db8:0000:0000:0000:8a2e:0370:7334'
          endIpAddress: '2001:db8::8a2e:370:9000'
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_missing_startIpAddress_and_endIpAddress 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for missing startIpAddress and endIpAddress'
  resource Compliant_Should_NOT_raise_issue_for_missing_startIpAddress_and_endIpAddress_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for missing startIpAddress and endIpAddress/dummy'
    properties: {
      firewallRules: [
        {}
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_startIpAddress_as_not_StringLiteral 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for startIpAddress as not StringLiteral'
  resource Compliant_Should_NOT_raise_issue_for_startIpAddress_as_not_StringLiteral_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for startIpAddress as not StringLiteral/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: []
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_endIpAddress_as_not_StringLiteral 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for endIpAddress as not StringLiteral'
  resource Compliant_Should_NOT_raise_issue_for_endIpAddress_as_not_StringLiteral_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for endIpAddress as not StringLiteral/dummy'
    properties: {
      firewallRules: [
        {
          endIpAddress: []
        }
      ]
    }
  }
}

resource Compliant_Should_NOT_raise_issue_for_startIpAddress_endIpAddress_as_not_StringLiteral 'Microsoft.Blockchain/blockchainMembers@dummy' = {
  name: 'Compliant: Should NOT raise issue for startIpAddress & endIpAddress as not StringLiteral'
  resource Compliant_Should_NOT_raise_issue_for_startIpAddress_endIpAddress_as_not_StringLiteral_dummy 'transactionNodes@dummy' = {
    name: 'Compliant: Should NOT raise issue for startIpAddress & endIpAddress as not StringLiteral/dummy'
    properties: {
      firewallRules: [
        {
          startIpAddress: {}
          endIpAddress: []
        }
      ]
    }
  }
}
