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
	"fmt"
	iac_helm "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/org.sonarsource.iac.helm"
	"google.golang.org/protobuf/proto"
	"os"
	"sigs.k8s.io/yaml"
	"strings"
	"text/template"
)

func main() {
	fmt.Fprintln(os.Stderr, "Starting sonar-helm-for-iac")
	scanner := bufio.NewScanner(os.Stdin)
	var stdinReader iac_helm.InputReader = iac_helm.StdinReader{}
	contents := stdinReader.ReadInput(scanner)
	if len(contents) == 0 {
		fmt.Fprintf(os.Stderr, "Received empty input, exiting\n")
		return
	} else if len(contents) != 2 {
		fmt.Fprintf(os.Stderr, "Expected 2 files, received %d (values.yaml missing?)\n", len(contents))
		os.Exit(1)
	}

	path := contents[0].Name
	rawTemplate := contents[0].Content
	rawValues := contents[1].Content
	fmt.Fprintf(os.Stderr, "Read in total %d characters from stdin; evaluating template <%s>\n", len(rawTemplate)+len(rawValues), path)

	evaluatedTemplate, err := evaluateTemplateInternal(path, rawTemplate, rawValues)
	result, err := toProtobuf(evaluatedTemplate, err)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to serialize evaluated template to Protobuf: %s\n", err.Error())
		os.Exit(1)
	}
	fmt.Fprintf(os.Stderr, "Writing %d bytes to stdout\n", len(result))
	os.Stdout.Write(result)
}

var handles []*template.Template

// For tests, the C code doesn't work in tests
func evaluateTemplateInternal(path string, content string, valuesFileContent string) (string, error) {
	templateId, err := newHandleID(path, content)
	if err != nil {
		return "", err
	}
	return executeWithValues(templateId, valuesFileContent)
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
