�
5type.googleapis.com/org.sonarsource.iac.helm.TextNode��apiVersion: v1
kind: Pod
metadata:
  name: example
spec:
  containers:
    - name: web
      image: nginx
      ports:
        - name: web
          containerPort: 80
          protocol: �
7type.googleapis.com/org.sonarsource.iac.helm.ActionNode��"��2X�Q
6type.googleapis.com/org.sonarsource.iac.helm.FieldNode�Valuesprotocol2��M
;type.googleapis.com/org.sonarsource.iac.helm.IdentifierNode	�defaultL
7type.googleapis.com/org.sonarsource.iac.helm.StringNode�"TCP""TCP2R�K
;type.googleapis.com/org.sonarsource.iac.helm.IdentifierNode	�quote�
5type.googleapis.com/org.sonarsource.iac.helm.TextNodeV�Q
      securityContext:
        allowPrivilegeEscalation: true # Sensitive S6428
