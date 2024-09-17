var toConnect1 = concat(
  [
    {
      name: 'web'
      network: '127.0.0.1'
    }
  ],
  [
    {
      name: 'prod'
      network: '10.0.0.1', port: 80
    }
  ])

var toConnect2 = concat([
  { name: 'web', network: '127.0.0.1' }
], [
  { name: 'prod', network: '10.0.0.1' }
])
