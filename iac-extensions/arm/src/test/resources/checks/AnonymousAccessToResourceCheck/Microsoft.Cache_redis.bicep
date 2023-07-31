resource appService 'Microsoft.Cache/redis@2022-09-01' = {
  name: 'example'
  properties: {
    redisConfiguration: {
      // Noncompliant@+1 {{Make sure that disabling authentication is safe here.}}
      authnotrequired: 'true'
    }
  }
}

resource appService 'Microsoft.Cache/redis@2022-09-01' = {
  name: 'example'
  properties: {
    redisConfiguration: {
      // boolean instead of string, invalid type for this property
      authnotrequired: true
    }
  }
}

resource appService 'Microsoft.Cache/redis@2022-09-01' = {
  name: 'example'
  properties: {
    redisConfiguration: {}
  }
}

resource appService 'Microsoft.Cache/redis@2022-09-01' = {
  name: 'example'
  properties: {}
}

resource appService 'Microsoft.Cache/redis@2022-09-01' = {
  name: 'example'
  properties: {
    redisConfiguration: {
      authnotrequired: 'false'
    }
  }
}
