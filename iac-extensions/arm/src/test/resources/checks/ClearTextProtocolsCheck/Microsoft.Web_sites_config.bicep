resource Raise_issue_as_ftpsState_is_set_to_AllAllowed 'Microsoft.Web/sites/config@2022-09-01' = {
  name: 'Raise issue as ftpsState is set to AllAllowed'
  properties: {
    ftpsState: 'AllAllowed' // Noncompliant{{Make sure that using clear-text protocols is safe here.}}
//  ^^^^^^^^^^^^^^^^^^^^^^^
  }
}

resource Compliant 'Microsoft.Web/sites/config@2022-09-01' = {
  name: 'Compliant'
  properties: {
    ftpsState: 'FtpsOnly'
  }
}

resource Compliant_with_wrong_format 'Microsoft.Web/sites/config@2022-09-01' = {
  name: 'Compliant with wrong format'
  properties: {
    ftpsState: true
  }
}
