---
title: Terraform/CloudFormation/Kubernetes
key: iac
---

<!-- static -->
<!-- update_center:iac -->
<!-- /static -->

## Language-Specific Properties

Discover and update the Terraform [properties](/analysis/analysis-parameters/) in: **<!-- sonarcloud -->Project <!-- /sonarcloud -->[Administration > General Settings > Languages > Terraform](/#sonarqube-admin#/admin/settings?category=Terraform)**

Discover and update the CloudFormation [properties](/analysis/analysis-parameters/) in: **<!-- sonarcloud -->Project <!-- /sonarcloud -->[Administration > General Settings > Languages > CloudFormation](/#sonarqube-admin#/admin/settings?category=CloudFormation)**

Discover and update the Kubernetes [properties](/analysis/analysis-parameters/) in: **<!-- sonarcloud -->Project <!-- /sonarcloud -->[Administration > General Settings > Languages > Kubernetes](/#sonarqube-admin#/admin/settings?category=Kubernetes)**


## Supported Versions, Formats and Providers
* Terraform 1.x (HCL format only)
* CloudFormation with AWSTemplateFormatVersion 2010-09-09 (YAML and JSON)
* Kubernetes (YAML)
* AWS, Azure and GCP

### Terraform provider versions

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

For CloudFormation you can import cfn-lint reports. See <!-- sonarcloud -->Project <!-- /sonarcloud -->**[Administration > General Settings > External Analyzers](/#sonarqube-admin#/admin/settings?category=external+analyzers)** for more information
