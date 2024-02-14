// SonarQube IaC Plugin
// Copyright (C) 2021-2024 SonarSource SA
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
	pbstructs "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/org.sonar.iac.helm"
	"github.com/stretchr/testify/assert"
	"google.golang.org/protobuf/proto"
	"os"
	"os/exec"
	"testing"
)

type InputReaderMock struct {
	Name     string
	Contents converters.Files
}

func (i *InputReaderMock) ReadInput(*bufio.Scanner) (string, converters.Files, error) {
	return i.Name, i.Contents, nil
}

type FailingProtobufSerializer struct{}

func (s FailingProtobufSerializer) Serialize(content string, ast *pbstructs.Tree, err error) ([]byte, error) {
	return nil, errors.New("serialization error")
}

var DefaultChartYaml = []byte(`
name: test-project
apiVersion: v3
`)

func Test_no_file_provided(t *testing.T) {
	err := validateInput(converters.Files{})

	assert.NotNil(t, err)
	assert.Equal(t, "no input received", err.Error())
}

func Test_only_one_file_provided(t *testing.T) {
	err := validateInput(converters.Files{
		"a.yaml": []byte("apiVersion: v1"),
	})

	assert.NoError(t, err)
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
	stdin.Write([]byte("foo.yaml\n1\napiVersion: v1\nvalues.yaml\n0\n\nEND\n"))
	err := cmd.Wait()

	var e *exec.ExitError
	errors.As(err, &e)
	assert.Equal(t, 1, e.ExitCode())
}

func Test_two_files_provided(t *testing.T) {
	stdinReader = &InputReaderMock{
		Name: "a.yaml",
		Contents: converters.Files{
			"templates/a.yaml": []byte("apiVersion: v1"),
			"values.yaml":      []byte("foo: bar"),
		},
	}

	main()

	// verify that main does not crash and this code is reached
	assert.Nil(t, nil)
}

func Test_evaluate_simple_template(t *testing.T) {
	template := []byte(`
apiVersion: v1
kind: Pod
metadata:
  name: example
  labels:
    app: {{ .Chart.Name }}
spec:
  containers:
    - name: web
      image: nginx
      ports:
        - name: web
          containerPort: {{ .Values.container.port }}
          protocol: TCP
`)

	values := []byte(`
container:
  port: 8080
`)

	expected := `
apiVersion: v1
kind: Pod
metadata:
  name: example
  labels:
    app: test-project
spec:
  containers:
    - name: web
      image: nginx
      ports:
        - name: web
          containerPort: 8080
          protocol: TCP
`

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml": template,
		"values.yaml":      values,
		"Chart.yaml":       DefaultChartYaml}))

	assert.NoError(t, result.Error)
	assert.Equal(t, expected, result.Template)
}

func Test_evaluate_template_missing_value(t *testing.T) {
	template := []byte(`
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
`)

	values := []byte(`
container: foo
`)

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml": template,
		"values.yaml":      values,
		"Chart.yaml":       DefaultChartYaml}))

	assert.Equal(t, "", result.Template)
	assert.Equal(t,
		"template: test-project/templates/a.yaml:12:35: executing \"test-project/templates/a.yaml\" at <.Values.container.port>: "+
			"can't evaluate field port in type interface {}",
		result.Error.Error())
}

func Test_evaluate_empty_values(t *testing.T) {
	template := []byte(`
apiVersion: v1
kind: Pod
metadata:
spec: {{ print "foo" }}
`)

	expected := `
apiVersion: v1
kind: Pod
metadata:
spec: foo
`

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml": template,
		"values.yaml":      make([]byte, 0),
		"Chart.yaml":       DefaultChartYaml}))

	assert.NoError(t, result.Error)
	assert.Equal(t, expected, result.Template)
}

func Test_evaluate_empty_additional_file(t *testing.T) {
	template := []byte(`
apiVersion: v1
kind: Pod
metadata:
spec: {{ print "foo" }}
`)

	values := []byte(`
container: foo
`)

	expected := `
apiVersion: v1
kind: Pod
metadata:
spec: foo
`

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml":     template,
		"templates/empty.yaml": make([]byte, 0),
		"values.yaml":          values,
		"Chart.yaml":           DefaultChartYaml}))

	assert.NoError(t, result.Error)
	assert.Equal(t, expected, result.Template)
}

func Test_evaluate_template_containing_sprig_functions(t *testing.T) {
	template := []byte(`
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
`)

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

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml": template,
		"values.yaml":      make([]byte, 0),
		"Chart.yaml":       DefaultChartYaml}))

	assert.NoError(t, result.Error)
	assert.Equal(t, expected, result.Template)
}

func Test_evaluate_template_extra_functions(t *testing.T) {
	template := []byte(`
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
`)

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

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml": template,
		"values.yaml":      make([]byte, 0),
		"Chart.yaml":       DefaultChartYaml}))

	assert.NoError(t, result.Error)
	assert.Equal(t, expected, result.Template)
}

func Test_evaluate_template_custom_functions(t *testing.T) {
	template := []byte(`
apiVersion: {{ lookup "v1" "Pod" "mynamespace" "mypod" }}
kind: Pod
metadata:
  app.kubernetes.io/version: {{ tpl .Values.image.tag . | quote }}
  include/init-sysctl: {{ include (print $.Template.BasePath "/init-sysctl.yaml") . }}
  checksum/init-sysctl: {{ include (print $.Template.BasePath "/init-sysctl.yaml") . | sha256sum }}
  foo: {{ required "A valid foo is required!" .Values.foo }}
  host: {{ getHostByName "www.google.com" }}
  len: {{ len .Values.foo }}
  anchovy-or-anchovies: {{ len .Values.foo | plural "one anchovy" "many anchovies" }}
{{- $myList := list 1 2 3 }}
  index0: {{ index $myList 0 }}
  urlquery: {{ urlquery "example.com/search?foo=bar" }}
  fail: {{ fail "Please do not fail" }}
spec:
{{- with .Values.words }}
  sentence: {{ join "," . }}
{{- end }}
`)

	values := []byte(`
image:
  tag: 1.0.0-{{ .Values.edition }}
edition: "community"
foo: foo-value
words: ["Hello", "World"]
`)

	expected := `
apiVersion: map[]
kind: Pod
metadata:
  app.kubernetes.io/version: "1.0.0-community"
  include/init-sysctl: init-sysctl
  checksum/init-sysctl: 5d1274616a37c4a59a71d7807619f69bc49fe86958e9c8e77bc0995a3b92fb56
  foo: foo-value
  host: 192.0.2.0
  len: 9
  anchovy-or-anchovies: many anchovies
  index0: 1
  urlquery: example.com%2Fsearch%3Ffoo%3Dbar
  fail: 
spec:
  sentence: Hello,World
`

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml":           template,
		"values.yaml":                values,
		"Chart.yaml":                 DefaultChartYaml,
		"templates/init-sysctl.yaml": []byte("init-sysctl")}))

	assert.NoError(t, result.Error)
	assert.Equal(t, expected, result.Template)
}

func Test_evaluate_template_conversion_functions(t *testing.T) {
	template := []byte(`
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
`)
	values := []byte(`
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
`)

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

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml": template,
		"values.yaml":      values,
		"Chart.yaml":       DefaultChartYaml}))

	assert.NoError(t, result.Error)
	assert.Equal(t, expected, result.Template)
}

func Test_evaluate_invalid_template(t *testing.T) {
	template := []byte(`
apiVersion: {{ hello
`)
	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml": template,
		"values.yaml":      make([]byte, 0),
		"Chart.yaml":       DefaultChartYaml}))

	assert.Equal(t, "", result.Template)
	assert.Equal(t, "template: test-project/templates/a.yaml:3: unclosed action started at test-project/templates/a.yaml:2", result.Error.Error())
}

func Test_evaluate_invalid_values(t *testing.T) {
	template := []byte(`
apiVersion: v1
`)
	values := []byte(`
foo: bar: baz
`)

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml": template,
		"values.yaml":      values,
		"Chart.yaml":       DefaultChartYaml}))

	assert.Equal(t, "", result.Template)
	assert.Error(t, result.Error)
	assert.Equal(t,
		"error parsing values file: error converting YAML to JSON: yaml: line 2: mapping values are not allowed in this context",
		result.Error.Error())
}

func Test_to_protobuf_valid(t *testing.T) {
	template := []byte("apiVersion: {{ .Values.api }}")
	values := []byte("api: v1")

	evaluatedTemplate := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml": template,
		"values.yaml":      values,
		"Chart.yaml":       DefaultChartYaml}))
	ast := converter.ConvertTree(string(template), evaluatedTemplate.Ast)
	result, _ := serializer.Serialize(evaluatedTemplate.Template, ast, evaluatedTemplate.Error)

	templateFromProto := &pbstructs.TemplateEvaluationResult{}
	_ = proto.Unmarshal(result, templateFromProto)

	assert.Equal(t, "apiVersion: v1", templateFromProto.Template)
	assert.Equal(t, "", templateFromProto.Error)
}

func Test_to_protobuf_invalid(t *testing.T) {
	template := []byte("apiVersion: {{ .Values.api")

	evaluatedTemplate := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml": template,
		"values.yaml":      make([]byte, 0),
		"Chart.yaml":       DefaultChartYaml}))
	ast := converter.ConvertTree(string(template), evaluatedTemplate.Ast)
	result, _ := serializer.Serialize(evaluatedTemplate.Template, ast, evaluatedTemplate.Error)

	templateFromProto := &pbstructs.TemplateEvaluationResult{}
	_ = proto.Unmarshal(result, templateFromProto)

	assert.Equal(t, "", templateFromProto.Template)
	assert.Equal(t, "template: test-project/templates/a.yaml:1: unclosed action", templateFromProto.Error)
}

func Test_evaluate_template_default_function(t *testing.T) {
	template := []byte(`
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
  `)

	values := []byte(`
protocol: UDP
`)

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

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml": template,
		"values.yaml":      values,
		"Chart.yaml":       DefaultChartYaml}))

	assert.NoError(t, result.Error)
	assert.Equal(t, expected, result.Template)
}

func Test_template_struct_from_2_sources(t *testing.T) {
	sources := converters.Files{
		"templates/a.yaml": []byte("apiVersion: v1"),
		"values.yaml":      []byte("foo: bar"),
	}

	templateSources := NewTemplateSourcesFromRawSources("a.yaml", sources)

	assert.Equal(t, 2, templateSources.NumSources())
}

func Test_template_struct_from_3_sources(t *testing.T) {
	sources := converters.Files{
		"templates/a.yaml": []byte("apiVersion: v1"),
		"_helpers.tpl":     []byte("{{/* comment */}}"),
		"values.yaml":      []byte("foo: bar"),
	}

	templateSources := NewTemplateSourcesFromRawSources("a.yaml", sources)

	assert.Equal(t, 3, templateSources.NumSources())
	assert.Equal(t, "foo: bar", templateSources.Values())
}

func Test_evaluate_template_files_function(t *testing.T) {
	template := []byte(`
apiVersion: v1
kind: ConfigMap
data:
  {{ range .Files.Lines "config.properties" }}{{ . }}{{ end }}
`)

	values := []byte(`
protocol: UDP
`)

	config := []byte(`
org.example.prop=true
`)

	expected := `
apiVersion: v1
kind: ConfigMap
data:
  org.example.prop=true
`

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml":  template,
		"values.yaml":       values,
		"Chart.yaml":        DefaultChartYaml,
		"config.properties": config}))

	assert.NoError(t, result.Error)
	assert.Equal(t, expected, result.Template)
}

func Test_evaluate_template_include_function(t *testing.T) {
	template := []byte(`foo: {{ include "app.name" $ }}`)

	values := []byte(`bar: 1`)

	helpers := []byte(`
{{- define "app.name" -}}
{{- printf "My application name" -}}
{{- end -}}`)

	expected := `foo: My application name`

	fileNameToFileContent := converters.Files{
		"templates/a.yaml":       template,
		"values.yaml":            values,
		"templates/_helpers.tpl": helpers,
		"Chart.yaml":             DefaultChartYaml}
	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", fileNameToFileContent))

	assert.NoError(t, result.Error)
	assert.Equal(t, expected, result.Template)
}

func Test_tpl_with_errors(t *testing.T) {
	template := []byte(`
apiVersion: {{ tpl .Values.api . | quote }}
`)

	values := []byte(`
api: v1-{{ }}
`)

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml": template,
		"values.yaml":      values,
		"Chart.yaml":       DefaultChartYaml}))

	assert.Equal(t, "", result.Template)
	assert.Error(t, result.Error)
	assert.Equal(t, "template: test-project/templates/a.yaml:2:15: executing \"test-project/templates/a.yaml\" at <tpl .Values.api .>: error calling tpl: "+
		"template: test-project/templates/a.yaml:1: missing value for command", result.Error.Error())
}

func Test_tpl_cross_file(t *testing.T) {
	template := []byte(`
apiVersion: {{ template "my-tpl" . }}
`)

	values := []byte(`
api: v1
`)

	helpers := []byte(`
{{- define "my-tpl" -}}
{{ tpl .Values.api . | quote }}
{{- end -}}
`)

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml":       template,
		"values.yaml":            values,
		"Chart.yaml":             DefaultChartYaml,
		"templates/_helpers.tpl": helpers}))

	assert.Equal(t, "\napiVersion: \"v1\"\n", result.Template)
	assert.Nil(t, result.Error)
}

func Test_tpl_cross_file_invalid_included_template(t *testing.T) {
	template := []byte(`
apiVersion: {{ template "my-tpl" . }}
`)

	values := []byte(`
api: v1
`)

	helpers := []byte(`
{{- define "my-tpl" -}}
{{ .Values.api
{{- end -}}
`)

	result := evaluateTemplate(converters.NewTemplateSources("templates/a.yaml", converters.Files{
		"templates/a.yaml":       template,
		"values.yaml":            values,
		"Chart.yaml":             DefaultChartYaml,
		"templates/_helpers.tpl": helpers}))

	assert.Error(t, result.Error)
	assert.Equal(t, "template: test-project/templates/_helpers.tpl:4: unexpected \"{\" in operand", result.Error.Error())
}
