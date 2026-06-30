param ipRangeParameter string

resource aksPublicRange 'Microsoft.ContainerService/managedClusters@2024-02-01' = {
  name: 'aks-public-range'
  properties: {
    apiServerAccessProfile: {
      authorizedIPRanges: [
        '8.8.8.8' // Noncompliant{{Make sure that allowing public IP addresses is safe here.}}
//      ^^^^^^^^^
      ]
    }
  }
}

// 0.0.0.0/32 tells AKS to allow only the outbound public IP of the Standard SKU load balancer.
resource aksSentinel 'Microsoft.ContainerService/managedClusters@2024-02-01' = {
  name: 'aks-sentinel'
  properties: {
    apiServerAccessProfile: {
      authorizedIPRanges: [
        '0.0.0.0/32'
      ]
    }
  }
}

resource aksPrivateRanges 'Microsoft.ContainerService/managedClusters@2024-02-01' = {
  name: 'aks-private-ranges'
  properties: {
    apiServerAccessProfile: {
      authorizedIPRanges: [
        '10.0.0.0/8'
        '172.16.0.0/12'
        '192.168.0.0/16'
      ]
    }
  }
}

// Mixed list: only the public entry is flagged, the sentinel is left untouched.
resource aksMixed 'Microsoft.ContainerService/managedClusters@2024-02-01' = {
  name: 'aks-mixed'
  properties: {
    apiServerAccessProfile: {
      authorizedIPRanges: [
        '0.0.0.0/32'
        '12.34.56.78/24' // Noncompliant
      ]
    }
  }
}

// Non-string-literal entries (parameters, references) are left untouched.
resource aksParameterized 'Microsoft.ContainerService/managedClusters@2024-02-01' = {
  name: 'aks-parameterized'
  properties: {
    apiServerAccessProfile: {
      authorizedIPRanges: [
        ipRangeParameter
      ]
    }
  }
}

// No apiServerAccessProfile / authorizedIPRanges at all: nothing to classify here.
resource aksNoProfile 'Microsoft.ContainerService/managedClusters@2024-02-01' = {
  name: 'aks-no-profile'
  properties: {}
}
