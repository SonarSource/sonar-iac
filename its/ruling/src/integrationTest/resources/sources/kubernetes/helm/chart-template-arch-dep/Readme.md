To unpack dependent Chart execute in  `chart-template-arch-dep/charts`:

```shell
cd charts
tar zxf common-0.0.1.tgz
rm common-0.0.1.tgz
```

To build dependent Chart execute in `chart-template-arch-dep/charts`:

```shell
cd charts
tar czf ../common-0.0.1.tgz common
mv ../common-0.0.1.tgz .
rm -rf common
```

