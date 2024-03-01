{{- define "securityContext" -}}
securityContext:
  privileged: false
  allowPrivilegeEscalation: false
  readOnlyRootFilesystem: false
  runAsNonRoot: false
  runAsUser: 0
  capabilities:
    drop:
    - ALL
    add:
    - CHOWN
    - FSETID
    - FOWNER
    - SETGID
    - SETUID
    - DAC_OVERRIDE
{{- end -}}
