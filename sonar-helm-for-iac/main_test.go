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
	"bufio"
	"errors"
	"github.com/SonarSource/sonar-iac/sonar-helm-for-iac/converters"
	iac_helm "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/org.sonarsource.iac.helm"
	"google.golang.org/protobuf/proto"
	"os"
	"os/exec"
	"testing"

	"github.com/stretchr/testify/assert"
)

type InputReaderMock struct {
	Contents []converters.Content
}

func (i *InputReaderMock) ReadInput(*bufio.Scanner) ([]converters.Content, error) {
	return i.Contents, nil
}

type FailingProtobufSerializer struct{}

func (s FailingProtobufSerializer) Serialize(content string, err error) ([]byte, error) {
	return nil, errors.New("serialization error")
}

func Test_no_file_provided(t *testing.T) {
	err := validateContents([]converters.Content{})

	assert.NotNil(t, err)
	assert.Equal(t, "no input received", err.Error())
}

func Test_only_one_file_provided(t *testing.T) {
	err := validateContents([]converters.Content{
		{
			Name:    "a.yaml",
			Content: "apiVersion: v1",
		},
	})

	assert.NotNil(t, err)
	assert.Equal(t, "expected 2 files, received 1 files, possible missing values file", err.Error())
}

func Test_exit_code_with_serialization_error(t *testing.T) {
	if os.Getenv("BE_CRASHER") == "1" {
		serializer = FailingProtobufSerializer{}
		main()
		return
	}
	cmd := exec.Command(os.Args[0], "-test.run=Test_exit_code_with_serialization_error")
	cmd.Env = append(os.Environ(), "BE_CRASHER=1")
	stdin, _ := cmd.StdinPipe()
	defer stdin.Close()
	cmd.Start()
	stdin.Write([]byte("foo.yaml\n1\napiVersion: v1\nvalues.yaml\n0\nEND\n"))
	err := cmd.Wait()

	var e *exec.ExitError
	errors.As(err, &e)
	assert.Equal(t, 1, e.ExitCode())
}

func Test_two_files_provided(t *testing.T) {
	stdinReader = &InputReaderMock{
		Contents: []converters.Content{
			{
				Name:    "a.yaml",
				Content: "apiVersion: v1",
			},
			{
				Name:    "values.yaml",
				Content: "foo: bar",
			},
		},
	}

	main()

	// verify that main does not crash and this code is reached
	assert.Nil(t, nil)
}

func Test_evaluate_simple_template(t *testing.T) {
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

	result, _ := evaluateTemplateInternal("a.yaml", template, values)

	assert.Equal(t, expected, result)
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

func Test_evaluate_template_conversion_functions(t *testing.T) {
	template := `
tolerations:
{{ toYaml .Values.tolerations | indent 2 }}
  {{- with .Values.missingValue -}}
  {{ toYaml . | nindent 2 }}
  {{- end }}
{{- $person := .Values.person | toYaml | fromYaml }}
fromYamlExample: "My name is {{ $person.name }} and I am {{ $person.age }} years old"
fromYamlError: {{ fromYaml "abc" }} 
{{- $personArray := .Values.personArray | fromYamlArray }}
fromYamlArrayExample:
{{- range $p := $personArray }}
  - {{ $p | quote}}
{{- end }}
fromYamlArrayError: {{ fromYamlArray "{" }}
toJsonExample: {{ toJson .Values.person }}
{{- $personFromJson := fromJson .Values.personJson }}
fromJsonExample: "My name is {{ $personFromJson.name }} and I am {{ $personFromJson.age }} years old"
fromJsonError: {{ fromJson "}" }}
fromJsonArrayExample:
{{- $fromJsonArray := .Values.personJsonArray | fromJsonArray }}
{{- range $i := $fromJsonArray }}
  - {{ $i | quote }}
{{- end }}
fromJsonArrayError: {{ fromJsonArray "{" }}
toTomlExample: {{ .Values.person | toToml | quote }}
`
	values := `
tolerations:
  - key: "sonarqube"
    operator: "Equal"
    value: "true"
    effect: "NoSchedule"
person:
  name: Bob
  age: 25
personArray: "[Alice, Bob]"
personJson: '{"name": "Json", "age": 20}'
personJsonArray: '["Json", "Gson", "Yaml"]'
`

	expected := `
tolerations:
  - effect: NoSchedule
    key: sonarqube
    operator: Equal
    value: "true"
fromYamlExample: "My name is Bob and I am 25 years old"
fromYamlError: map[Error:error unmarshaling JSON: while decoding JSON: json: cannot unmarshal string into Go value of type map[string]interface {}]
fromYamlArrayExample:
  - "Alice"
  - "Bob"
fromYamlArrayError: [error converting YAML to JSON: yaml: line 1: did not find expected node content]
toJsonExample: {"age":25,"name":"Bob"}
fromJsonExample: "My name is Json and I am 20 years old"
fromJsonError: map[Error:invalid character '}' looking for beginning of value]
fromJsonArrayExample:
  - "Json"
  - "Gson"
  - "Yaml"
fromJsonArrayError: [unexpected end of JSON input]
toTomlExample: "age = 25.0\nname = \"Bob\"\n"
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
		"error parsing values file: error converting YAML to JSON: yaml: line 2: mapping values are not allowed in this context",
		err.Error())
}

func Test_to_protobuf_valid(t *testing.T) {
	template := "apiVersion: {{ .Values.api }}"
	values := "api: v1"

	evaluatedTemplate, err := evaluateTemplateInternal("a.yaml", template, values)
	result, err := serializer.Serialize(evaluatedTemplate, err)

	templateFromProto := &iac_helm.TemplateEvaluationResult{}
	proto.Unmarshal(result, templateFromProto)

	assert.Equal(t, "apiVersion: v1", templateFromProto.Template)
	assert.Equal(t, "", templateFromProto.Error)
}

func Test_to_protobuf_invalid(t *testing.T) {
	template := "apiVersion: {{ .Values.api"

	evaluatedTemplate, err := evaluateTemplateInternal("a.yaml", template, "")
	result, err := serializer.Serialize(evaluatedTemplate, err)

	templateFromProto := &iac_helm.TemplateEvaluationResult{}
	proto.Unmarshal(result, templateFromProto)

	assert.Equal(t, "", templateFromProto.Template)
	assert.Equal(t, "template: a.yaml:1: unclosed action", templateFromProto.Error)
}

func Test_evaluate_template_default_function(t *testing.T) {
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
            containerPort: 80
            protocol: {{ .Values.protocol | default "TCP" | quote }}
  `

	values := `
protocol: UDP
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
            containerPort: 80
            protocol: "UDP"
  `

	result, _ := evaluateTemplateInternal("a.yaml", template, values)

	assert.Equal(t, expected, result)
}
