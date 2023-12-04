// SonarQube IaC Plugin
// Copyright (C) 2021-2023 SonarSource SA
// mailto:info AT sonarsource DOT com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

package main

import (
	"errors"
	iac_helm "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/org.sonarsource.iac.helm"
	"google.golang.org/protobuf/proto"
	"testing"

	"github.com/stretchr/testify/assert"
)

func Test_call_main(t *testing.T) {
	main()
}

func Test_evaluate_template(t *testing.T) {
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

	result := evaluateTemplateInGoTypes("a.yaml", template, values)
	templateFromProto := &iac_helm.TemplateEvaluationResult{}
	proto.Unmarshal([]byte(result), templateFromProto)

	assert.Equal(t, expected, templateFromProto.Template)
}

func Test_evaluate_template_missing_value(t *testing.T) {
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
container: foo
`

	result, err := evaluateTemplateInternal("a.yaml", template, values)

	assert.Equal(t, "", result)
	assert.Equal(t,
		"template: a.yaml:12:35: executing \"a.yaml\" at <.Values.container.port>: "+
			"can't evaluate field port in type interface {}",
		err.Error())
}

func Test_evaluate_template_containing_sprig_functions(t *testing.T) {
	template := `
apiVersion: v1
kind: {{ trim "  Pod  " }}
metadata:
  name: example
spec:
  containers:
    - name: {{ lower "WEB" }}
      image: {{ nospace "n g i  nx" }}
      ports:
        - name: {{ trunc 3 "website" }}
          containerPort: {{ repeat 2 "80" }}
          protocol: {{ upper "tcp" }}
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

	result, _ := evaluateTemplateInternal("a.yaml", template, "")

	assert.Equal(t, expected, result)
}

func Test_evaluate_template_extra_functions(t *testing.T) {
	template := `
apiVersion: v1
kind: Pod
metadata:
  name: example
spec:
  containers:
    - name: web
      image: {{ lookup "v1" "Pod" "mynamespace" "mypod" }}
      ports:
        - name: web
          containerPort: {{ getHostByName "www.google.com" }}
          protocol: TCP
`

	expected := `
apiVersion: v1
kind: Pod
metadata:
  name: example
spec:
  containers:
    - name: web
      image: map[]
      ports:
        - name: web
          containerPort: 192.0.2.0
          protocol: TCP
`

	result, _ := evaluateTemplateInternal("a.yaml", template, "")

	assert.Equal(t, expected, result)
}

func Test_evaluate_template_custom_functions(t *testing.T) {
	template := `
apiVersion: {{ lookup "v1" "Pod" "mynamespace" "mypod" }}
kind: Pod
metadata:
  app.kubernetes.io/version: {{ tpl .Values.image.tag . | quote }}
  include/init-sysctl: {{ include "/init-sysctl.yaml" . }}
  checksum/init-sysctl: {{ include "/init-sysctl.yaml" . | sha256sum }}
  foo: {{ required "A valid foo is required!" .Values.foo }}
  host: {{ getHostByName "www.google.com" }}
  len: {{ len .Values.foo }}
  anchovy-or-anchovies: {{ len .Values.foo | plural "one anchovy" "many anchovies" }}
{{- $myList := list 1 2 3 }}
  index0: {{ index $myList 0 }}
  urlquery: {{ urlquery "example.com/search?foo=bar" }}
  fail: {{ fail "Please do not fail" }}
spec:
`

	values := `
image:
  tag: 1.0.0-{{ .Values.edition }}
edition: "community"
foo: foo-value
`

	expected := `
apiVersion: map[]
kind: Pod
metadata:
  app.kubernetes.io/version: "sonar-generated-tpl-0"
  include/init-sysctl: sonar-generated-include-1
  checksum/init-sysctl: 7853347def13052a4585b34d7b152b3d43c90f09071f94e8ffeca7c825b804bd
  foo: foo-value
  host: 192.0.2.0
  len: 9
  anchovy-or-anchovies: many anchovies
  index0: 1
  urlquery: example.com%2Fsearch%3Ffoo%3Dbar
  fail: 
spec:
`

	result, err := evaluateTemplateInternal("a.yaml", template, values)

	assert.Equal(t, expected, result)
	assert.Equal(t, nil, err)
}

func Test_evaluate_invalid_template(t *testing.T) {
	template := `
apiVersion: {{ hello
`
	result, err := evaluateTemplateInternal("a.yaml", template, "")

	assert.Equal(t, "", result)
	assert.Equal(t, "template: a.yaml:2: function \"hello\" not defined", err.Error())
}

func Test_evaluate_invalid_values(t *testing.T) {
	template := `
apiVersion: v1
`
	values := `
foo: bar: baz
`

	result, err := evaluateTemplateInternal("a.yaml", template, values)

	assert.Equal(t, "", result)
	assert.Equal(t,
		"error converting YAML to JSON: yaml: line 2: mapping values are not allowed in this context",
		err.Error())
}

func Test_to_protobuf_valid(t *testing.T) {
	template := "apiVersion: {{ .Values.api }}"
	values := "api: v1"

	evaluatedTemplate, err := evaluateTemplateInternal("a.yaml", template, values)
	result, err := toProtobuf(evaluatedTemplate, err)

	templateFromProto := &iac_helm.TemplateEvaluationResult{}
	proto.Unmarshal(result, templateFromProto)

	assert.Equal(t, "apiVersion: v1", templateFromProto.Template)
	assert.Equal(t, "", templateFromProto.Error)
}

func Test_to_protobuf_invalid(t *testing.T) {
	template := "apiVersion: {{ .Values.api"

	evaluatedTemplate, err := evaluateTemplateInternal("a.yaml", template, "")
	result, err := toProtobuf(evaluatedTemplate, err)

	templateFromProto := &iac_helm.TemplateEvaluationResult{}
	proto.Unmarshal(result, templateFromProto)

	assert.Equal(t, "", templateFromProto.Template)
	assert.Equal(t, "template: a.yaml:1: unclosed action", templateFromProto.Error)
}

func Test_to_protobuf_error(t *testing.T) {
	template := "apiVersion: {{ .Values.api }}"
	values := "api: v1"

	evaluatedTemplate, err := evaluateTemplateInternal("a.yaml", template, values)
	templateSerialization := newTemplateSerialization(func(evaluatedTemplate string, err error) ([]byte, error) {
		return nil, errors.New("mock serialization error")
	})
	result, err := templateSerialization.toProtobuf(evaluatedTemplate, err)

	assert.Equal(t, []byte(nil), result)
	assert.Equal(t, "mock serialization error", err.Error())
}
