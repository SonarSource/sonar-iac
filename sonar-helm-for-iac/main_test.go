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
          containerPort:{{ getHostByName "www.google.com" }}
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
          containerPort:
          protocol: TCP
`

	result, _ := evaluateTemplateInternal("a.yaml", template, "")

	assert.Equal(t, expected, result)
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
