resource Compliant_Should_NOT_raise_issue_for_whole_range 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for whole range'
  properties: {
    firewallRules: [
      {
        startIpAddress: '0.0.0.0'
        endIpAddress: '255.255.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_missing_start 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue missing start'
  properties: {
    firewallRules: [
      {
        endIpAddress: '9.8.7.6'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_missing_end 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue missing end'
  properties: {
    firewallRules: [
      {
        startIpAddress: '241.0.0.0'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IPs_10_0_0_0 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs < 10.0.0.0'
  properties: {
    firewallRules: [
      {
        startIpAddress: '1.2.3.4'
        endIpAddress: '9.255.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IPs_10_255_255_255_and_100_64_0_0 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 10.255.255.255 and < 100.64.0.0'
  properties: {
    firewallRules: [
      {
        startIpAddress: '11.0.0.0'
        endIpAddress: '100.63.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IPs_100_127_255_255_and_169_254_0_0 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 100.127.255.255 and < 169.254.0.0'
  properties: {
    firewallRules: [
      {
        startIpAddress: '100.128.0.0'
        endIpAddress: '169.253.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IPs_169_254_255_255_and_172_16_0_0 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 169.254.255.255 and < 172.16.0.0'
  properties: {
    firewallRules: [
      {
        startIpAddress: '169.255.0.0'
        endIpAddress: '172.15.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IPs_172_31_255_255_and_192_0_0_0 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 172.31.255.255 and < 192.0.0.0'
  properties: {
    firewallRules: [
      {
        startIpAddress: '172.32.0.0'
        endIpAddress: '191.255.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IPs_192_0_0_255_and_192_0_2_0 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 192.0.0.255 and < 192.0.2.0'
  properties: {
    firewallRules: [
      {
        startIpAddress: '192.0.1.0'
        endIpAddress: '192.0.1.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IPs_192_0_2_255_and_192_168_0_0 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 192.0.2.255 and < 192.168.0.0'
  properties: {
    firewallRules: [
      {
        startIpAddress: '192.0.3.0'
        endIpAddress: '192.167.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IPs_192_168_255_255_and_198_18_0_0 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 192.168.255.255 and < 198.18.0.0'
  properties: {
    firewallRules: [
      {
        startIpAddress: '192.169.0.0'
        endIpAddress: '192.17.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IPs_198_19_255_255_and_198_51_100_0 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 198.19.255.255 and < 198.51.100.0'
  properties: {
    firewallRules: [
      {
        startIpAddress: '198.20.0.0'
        endIpAddress: '198.51.99.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IPs_198_51_100_255_and_203_0_113_0 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 198.51.100.255 and < 203.0.113.0'
  properties: {
    firewallRules: [
      {
        startIpAddress: '198.51.101.0'
        endIpAddress: '203.0.112.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IPs_203_0_113_255_and_240_0_0_0 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 203.0.113.255 and < 240.0.0.0'
  properties: {
    firewallRules: [
      {
        startIpAddress: '203.0.114.0'
        endIpAddress: '239.255.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IPs_240_255_255_254_and_255_255_255_255 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 240.255.255.254 and < 255.255.255.255'
  properties: {
    firewallRules: [
      {
        startIpAddress: '240.255.255.255'
        endIpAddress: '255.255.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IPs_240_255_255_254_no_endIpAddress 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 240.255.255.254 no endIpAddress'
  properties: {
    firewallRules: [
      {
        startIpAddress: '240.255.255.255'
      }
    ]
  }
}

resource unknown_type_Compliant_Should_NOT_raise_issue_for_IPs_192_0_0_0_and_192_0_2_255_IP_ranges_edges_for_2_different_groups_between_are_public_addresses 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 192.0.0.0 and < 192.0.2.255 (IP ranges edges for 2 different groups, between are public addresses)'
  properties: {
    firewallRules: [
      {
        startIpAddress: '192.0.0.0'
        endIpAddress: '192.0.2.255'
      }
    ]
  }
}

resource unknown_type_Compliant_Should_NOT_raise_issue_for_IPs_192_0_0_0_and_192_0_2_255_IP_ranges_edges_for_2_different_groups_between_are_public_addresses 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IPs > 192.0.0.0 and < 192.0.2.255 (IP ranges edges for 2 different groups, between are public addresses)'
  properties: {
    firewallRules: [
      {
        startIpAddress: '192.0.0.0'
        endIpAddress: '192.0.2.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_unknown_startIpAddress 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for unknown startIpAddress'
  properties: {
    firewallRules: [
      {
        startIpAddress: 'unknown'
        endIpAddress: '250.0.0.0'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_unknown_endIpAddress 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for unknown endIpAddress'
  properties: {
    firewallRules: [
      {
        startIpAddress: '250.0.0.0'
        endIpAddress: 'unknown'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_private_networks_only_1 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for private networks only 1'
  properties: {
    firewallRules: [
      {
        startIpAddress: '10.0.0.0'
        endIpAddress: '10.255.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_used_within_an_ISP_s_network_for_carrier_grade_NAT 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for used within an ISP’s network for carrier-grade NAT'
  properties: {
    firewallRules: [
      {
        startIpAddress: '100.64.0.0'
        endIpAddress: '100.127.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_link_local_addresses_for_DHCP_failures 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for link-local addresses for DHCP failures'
  properties: {
    firewallRules: [
      {
        startIpAddress: '169.254.0.0'
        endIpAddress: '169.254.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_private_networks_only_2 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for private networks only 2'
  properties: {
    firewallRules: [
      {
        startIpAddress: '172.16.0.0'
        endIpAddress: '172.31.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_IETF_protocol_assignments 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for IETF protocol assignments'
  properties: {
    firewallRules: [
      {
        startIpAddress: '192.0.0.0'
        endIpAddress: '192.0.0.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_documentation_use_1 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for documentation use 1'
  properties: {
    firewallRules: [
      {
        startIpAddress: '192.0.2.0'
        endIpAddress: '192.0.2.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_private_networks_only_3 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for private networks only 3'
  properties: {
    firewallRules: [
      {
        startIpAddress: '192.168.0.0'
        endIpAddress: '192.168.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_inter_subnet_benchmarking 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for inter-subnet benchmarking'
  properties: {
    firewallRules: [
      {
        startIpAddress: '198.18.0.0'
        endIpAddress: '198.19.255.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_documentation_use_2 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for documentation use 2'
  properties: {
    firewallRules: [
      {
        startIpAddress: '198.51.100.0'
        endIpAddress: '198.51.100.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_documentation_use_3 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for documentation use 3'
  properties: {
    firewallRules: [
      {
        startIpAddress: '203.0.113.0'
        endIpAddress: '203.0.113.255'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_future_use 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for future use'
  properties: {
    firewallRules: [
      {
        startIpAddress: '240.0.0.0'
        endIpAddress: '240.255.255.254'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_reserved_for_future_use_part_of_range 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for reserved for future use (part of range)'
  properties: {
    firewallRules: [
      {
        startIpAddress: '240.1.2.3'
        endIpAddress: '240.100.101.102'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_unknown_value 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for unknown value'
  properties: {
    firewallRules: [
      {
        startIpAddress: 'unknown'
        endIpAddress: 'unknown'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_IP_6 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for IP 6'
  properties: {
    firewallRules: [
      {
        startIpAddress: '2001:0db8:0000:0000:0000:8a2e:0370:7334'
        endIpAddress: '2001:db8::8a2e:370:9000'
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_missing_startIpAddress_and_endIpAddress 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for missing startIpAddress and endIpAddress'
  properties: {
    firewallRules: [
      {}
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_startIpAddress_as_not_StringLiteral 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for startIpAddress as not StringLiteral'
  properties: {
    firewallRules: [
      {
        startIpAddress: []
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_endIpAddress_as_not_StringLiteral 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for endIpAddress as not StringLiteral'
  properties: {
    firewallRules: [
      {
        endIpAddress: []
      }
    ]
  }
}

resource Compliant_Should_NOT_raise_issue_for_startIpAddress_endIpAddress_as_not_StringLiteral 'unknown-type@dummy' = {
  name: 'Compliant: Should NOT raise issue for startIpAddress & endIpAddress as not StringLiteral'
  properties: {
    firewallRules: [
      {
        startIpAddress: {}
        endIpAddress: []
      }
    ]
  }
}