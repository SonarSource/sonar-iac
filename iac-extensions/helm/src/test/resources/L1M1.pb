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
          containerPort: �
7type.googleapis.com/org.sonarsource.iac.helm.ActionNodem�"f�2_�X
6type.googleapis.com/org.sonarsource.iac.helm.FieldNode�Values	containerport�
5type.googleapis.com/org.sonarsource.iac.helm.TextNode���  # simple Helm template
          protocol: TCP
      securityContext:
        allowPrivilegeEscalation: true # Sensitive S6428
