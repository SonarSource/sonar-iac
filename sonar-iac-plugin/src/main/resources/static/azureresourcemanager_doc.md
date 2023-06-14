---
title: AzureResourceManager
key: azureresourcemanager
---

## Supported Versions

| Format | Version          | Status              |
|--------|------------------|---------------------|
| JSON   | up to 2023-06-01 | Partially Supported |
| Bicep  | any              | Not Supported       |

## Language-Specific Properties

Discover and update the Azure Resource Manager [properties](/analysis/analysis-parameters/) in: **<!-- sonarcloud -->Project <!-- /sonarcloud -->[Administration > General Settings > Languages > AzureResourceManager](/#sonarqube-admin#/admin/settings?category=AzureResourceManager)**

## Relevant Limitations

### No NoSonar Support

The comments are not permitted in JSON files. For this reason, our parser does not support `NOSONAR` comments to suppress issues. Issues and hotspots must be reviewed in the UI.

