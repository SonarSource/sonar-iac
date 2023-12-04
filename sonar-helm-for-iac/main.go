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

import "C"
import (
	"fmt"
	iac_helm "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/org.sonarsource.iac.helm"
	"google.golang.org/protobuf/proto"
	"sigs.k8s.io/yaml"
	"strings"
	"text/template"
	"unsafe"
)

func main() {
	fmt.Println("Hello World!")
}

var handles []*template.Template

//export EvaluateTemplate
func EvaluateTemplate(path string, content string, valuesFileContent string) (unsafe.Pointer, C.int) {
	evaluatedTemplate, err := evaluateTemplateInternal(path, content, valuesFileContent)
	result, err := toProtobuf(evaluatedTemplate, err)
	if err != nil {
		fmt.Println("Failed to serialize evaluated template to Protobuf for " + path + " error: " + err.Error())
		return nil, C.int(0)
	}
	return C.CBytes(result), C.int(len(result))
}

// For tests, the C code doesn't work in tests
func evaluateTemplateInternal(path string, content string, valuesFileContent string) (string, error) {
	templateId, err := newHandleID(path, content)
	if err != nil {
		return "", err
	}
	return executeWithValues(templateId, valuesFileContent)
}

// also for tests, but the other way around
func evaluateTemplateInGoTypes(path string, content string, valuesFileContent string) ([]byte, int) {
	result, length := EvaluateTemplate(path, content, valuesFileContent)
	if result == nil {
		return nil, 0
	}
	return C.GoBytes(result, length), int(length)
}

func toProtobuf(evaluatedTemplate string, err error) ([]byte, error) {
	errorText := ""
	if err != nil {
		errorText = err.Error()
	}
	message := iac_helm.TemplateEvaluationResult{
		Template: evaluatedTemplate,
		Error:    errorText,
	}
	return proto.Marshal(&message)
}

// Create a template with name and expression and return its handle (a numeric ID to access the template later)
func newHandleID(name string, content string) (int, error) {
	t := template.New(name)
	t.Funcs(*addCustomFunctions())
	t, err := t.Parse(content)
	if err != nil {
		return -1, err
	}

	handles = append(handles, t)
	return len(handles) - 1, nil
}

func executeWithValues(templateId int, valuesFileContent string) (string, error) {
	valuesMap, err := yamlToMap(valuesFileContent)
	if err != nil {
		return "", err
	}
	vals := struct {
		Values map[string]interface{}
	}{valuesMap}

	tmpl := handles[templateId]
	var buf strings.Builder
	err = tmpl.Execute(&buf, vals)
	if err != nil {
		return "", err
	}
	return buf.String(), nil
}

func yamlToMap(input string) (map[string]interface{}, error) {
	vals := map[string]interface{}{}
	err := yaml.Unmarshal([]byte(input), &vals)
	if len(vals) == 0 {
		vals = map[string]interface{}{}
	}
	return vals, err
}
