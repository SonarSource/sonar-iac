---
title: Terraform
key: terraform
---

## Supported Versions

| Format | Version   | Status          |
|--------|-----------|-----------------|
| HCL    | 1.x       | Fully Supported |
| HCL    | 2.x       | Fully Supported |
| JSON   | any       | Not Supported   |

## Language-Specific Properties

Discover and update the Terraform [properties](/analysis/analysis-parameters/) in: **<!-- sonarcloud -->Project <!-- /sonarcloud -->[Administration > General Settings > Languages > Terraform](/#sonarqube-admin#/admin/settings?category=Terraform)**

## Supported Providers

* Amazon Web Services
* Azure Cloud
* Google Cloud Platform

### Provider versions

The respective Terraform providers are frequently updated. New resources, properties and default values are added. At the same time, others are deprecated or dropped. For this reason, the Terraform analysis is defensive by default: some issues will be automatically silenced to avoid raising false positives. In order to get a more precise analysis you can specify the provider versions your code supports via a parameter.

**AWS**: `sonar.terraform.provider.aws.version`<br>
**Azure**: `sonar.terraform.provider.azure.version`<br>
**GCP**: For Google Cloud Platform, no versions are currently considered in the analysis

Accepted are versions having the format: `X.Y.Z`, `X.Y` or `X`

Examples:

* `sonar.terraform.provider.aws.version=1.93.4`
* `sonar.terraform.provider.aws.version=3.4`
* `sonar.terraform.provider.aws.version=4`

## Related Pages

* [External Analyzer Reports](/#sonarqube-admin#/admin/settings?category=external+analyzers) ([TFLint](https://github.com/terraform-linters/tflint))
