{{- define "test.declarations" -}}
{{- $my_Variable := .Values.myVariableValue }}
{{- $ := .Values.myVariableValue }}
{{- $myVariable2 := .Values.myVariableValue }}

{{- range $KeyNc, $VAL_NC := .Values.favorite }}
  {{ $KeyNc }}:
  {{ $NON_COMPLIANT := "foo" }}
  - {{ $VAL_NC | quote }}
  {{ $VAL_NC = "foo" }}
{{- end }}

{{- range $keyNc, $valNc := .Values.favorite }}
  {{ $keyNc }}: {{ $valNc | quote }}
{{- end }}
{{- end }}
