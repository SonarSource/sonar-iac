resource templateDebug1 'Microsoft.Resources/deployments@2019-05-01' = {
  name: 'templateDebug1'
  properties: {
    debugSetting: { // Noncompliant {{Make sure this debug feature is deactivated before delivering the code in production.}}
//  [sc=4;el=+2;ec=6]^
      detailLevel: 'RequestContent, ResponseContent'
    }
  }
}

resource templateDebug2 'Microsoft.Resources/deployments@2019-05-01' = {
  name: 'templateDebug2'
  properties: {
    debugSetting: { // Noncompliant
      detailLevel: 'RequestContent'
    }
  }
}

resource templateDebug3 'Microsoft.Resources/deployments@2019-05-01' = {
  name: 'templateDebug3'
  properties: {
    debugSetting: { // Noncompliant
      detailLevel: 'Something that contain ResponseContent anywhere'
    }
  }
}

resource templateDebug4 'Microsoft.Resources/deployments@2019-05-01' = {
  name: 'templateDebug4'
  properties: {
    debugSetting: {}
  }
}

resource templateDebug5 'Microsoft.Resources/deployments@2019-05-01' = {
  name: 'templateDebug5'
  properties: {
    debugSetting: {
      detailLevel: 'other value'
    }
  }
}

resource templateDebug6 'Microsoft.Resources/deployments@2019-05-01' = {
  name: 'templateDebug6'
  properties: {
    debugSetting: {
      detailLevel: null
    }
  }
}

resource templateDebug7 'Microsoft.Resources/deployments@2019-05-01' = {
  name: 'templateDebug7'
  properties: {
    debugSetting: {
      detailLevel: 6
    }
  }
}
