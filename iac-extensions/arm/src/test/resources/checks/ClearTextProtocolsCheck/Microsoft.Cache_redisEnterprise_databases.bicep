resource Raise_issue_as_clientProtocol_is_set_to_Plaintext 'Microsoft.Cache/redisEnterprise/databases@2022-09-01' = {
  name: 'Raise issue as clientProtocol is set to Plaintext'
  properties: {
    clientProtocol: 'Plaintext' // Noncompliant{{Make sure that using clear-text protocols is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource Compliant 'Microsoft.Cache/redisEnterprise/databases@2022-09-01' = {
  name: 'Compliant'
  properties: {
    clientProtocol: 'Encrypted'
  }
}

resource Compliant_with_wrong_format 'Microsoft.Cache/redisEnterprise/databases@2022-09-01' = {
  name: 'Compliant with wrong format'
  properties: {
    clientProtocol: true
  }
}
