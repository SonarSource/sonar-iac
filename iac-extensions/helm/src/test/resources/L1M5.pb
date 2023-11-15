n
5type.googleapis.com/org.sonarsource.iac.helm.TextNode53apiVersion: v1
kind: Pod
metadata:
  name: example
Â
3type.googleapis.com/org.sonarsource.iac.helm.IfNode≠
™
9"h92b9\
6type.googleapis.com/org.sonarsource.iac.helm.FieldNode"@Valuesserviceannotations*πVJ
5type.googleapis.com/org.sonarsource.iac.helm.TextNodeV
annotations:Ê
6type.googleapis.com/org.sonarsource.iac.helm.RangeNode´
®p"Ñp*
p$key*v$value2dÄ]
6type.googleapis.com/org.sonarsource.iac.helm.FieldNode#áValuesserviceannotations*öûC
5type.googleapis.com/org.sonarsource.iac.helm.TextNode
û
    ò
7type.googleapis.com/org.sonarsource.iac.helm.ActionNode]¶"V¶2O¶H
9type.googleapis.com/org.sonarsource.iac.helm.VariableNode¶$key@
5type.googleapis.com/org.sonarsource.iac.helm.TextNode≠: 
7type.googleapis.com/org.sonarsource.iac.helm.ActionNode¥≤"¨≤2Q≤J
9type.googleapis.com/org.sonarsource.iac.helm.VariableNode≤$value2RªK
;type.googleapis.com/org.sonarsource.iac.helm.IdentifierNode	ªquoteù
5type.googleapis.com/org.sonarsource.iac.helm.TextNode„€›
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
