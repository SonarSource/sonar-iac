n
5type.googleapis.com/org.sonarsource.iac.helm.TextNode53apiVersion: v1
kind: Pod
metadata:
  name: example
û
3type.googleapis.com/org.sonarsource.iac.helm.IfNodeÃ
À
9"h92b9\
6type.googleapis.com/org.sonarsource.iac.helm.FieldNode"@Valuesserviceannotations*PVJ
5type.googleapis.com/org.sonarsource.iac.helm.TextNodeV
annotations:œ
5type.googleapis.com/org.sonarsource.iac.helm.TextNodeâpÝ
spec:
  containers:
    - name: web
      image: nginx
      ports:
        - name: web
          containerPort: 80
          protocol: TCP
      securityContext:
        allowPrivilegeEscalation: true # Sensitive S6428
