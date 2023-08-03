resource Noncompliant_publicNetworkAccess_is_enabled 'Microsoft.DBforMySQL/flexibleServers@2022-09-30-preview' = {
  name: 'Noncompliant: publicNetworkAccess is enabled'
  properties: {
    network: {
      publicNetworkAccess: 'Enabled' // Noncompliant{{Make sure allowing public network access is safe here.}}
//    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    }
  }
}

resource Compliant_publicNetworkAccess_is_disabled 'Microsoft.DBforMySQL/flexibleServers@2022-09-30-preview' = {
  name: 'Compliant: publicNetworkAccess is disabled'
  properties: {
    network: {
      publicNetworkAccess: 'Disabled'
    }
  }
}

resource Compliant_publicNetworkAccess_is_unknown 'Microsoft.DBforMySQL/flexibleServers@2022-09-30-preview' = {
  name: 'Compliant: publicNetworkAccess is unknown'
  properties: {
    network: {
      publicNetworkAccess: 'unknown'
    }
  }
}

resource Compliant_publicNetworkAccess_is_not_a_String_Litaeral 'Microsoft.DBforMySQL/flexibleServers@2022-09-30-preview' = {
  name: 'Compliant: publicNetworkAccess is not a String Litaeral'
  properties: {
    network: {
      publicNetworkAccess: {}
    }
  }
}

resource Compliant_publicNetworkAccess_is_not_defined 'Microsoft.DBforMySQL/flexibleServers@2022-09-30-preview' = {
  name: 'Compliant: publicNetworkAccess is not defined'
  properties: {
    network: {}
  }
}

resource Compliant_property_defined_for_unknown_type 'unknown.type@2022-09-30-preview' = {
  name: 'Compliant: property defined for unknown type'
  properties: {
    network: {
      publicNetworkAccess: 'Enabled'
    }
  }
}
