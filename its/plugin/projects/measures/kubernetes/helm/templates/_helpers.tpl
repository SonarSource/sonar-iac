{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "myapp.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Expand the Application Image name.
*/}}
{{- define "myapp.image" -}}
{{- printf "%s:%s" .Values.ApplicationNodes.image.repository .Values.ApplicationNodes.image.tag }}
{{- end -}}
