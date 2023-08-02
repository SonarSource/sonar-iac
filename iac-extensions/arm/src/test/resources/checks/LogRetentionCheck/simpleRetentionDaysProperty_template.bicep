resource type1 '${type}@dummy' = {
  name: 'Noncompliant: Should raise issue on low retentionDays'
  properties: {
    retentionDays: 7
  }
}

resource type2 '${type}@dummy' = {
  name: 'Noncompliant: Should raise issue on undefined retentionDays'
  properties: {
  }
}

resource type3 '${type}@dummy' = {
  name: 'Compliant: retentionDays is zero (no limit)'
  properties: {
    retentionDays: 0
  }
}

resource type4 '${type}@dummy' = {
  name: 'Compliant: retentionDays is 15'
  properties: {
    retentionDays: 15
  }
}

resource type5 '${type}@dummy' = {
  name: 'Compliant: wrong format'
  properties: {
    retentionDays: 'not a number format'
  }
}
