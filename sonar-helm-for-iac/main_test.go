package main

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

func Test_evaluate_template(t *testing.T) {
	fmt.Println("Hello World from test!")
	template := `
apiVersion: v1
kind: Pod
metadata:
  name: example
spec:
  containers:
    - name: web
      image: nginx
      ports:
        - name: web
          containerPort: {{ .Values.container.port }}
          protocol: TCP
`

	values := `
container:
  port: 8080
`

	expected := `
apiVersion: v1
kind: Pod
metadata:
  name: example
spec:
  containers:
    - name: web
      image: nginx
      ports:
        - name: web
          containerPort: 8080
          protocol: TCP
`

	result := evaluateTemplateInternal("a.yaml", template, values)

	assert.Equal(t, result, expected)
}
