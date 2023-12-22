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
	"fmt"
	"github.com/SonarSource/sonar-iac/sonar-helm-for-iac/converters"
	"os"
	"sigs.k8s.io/yaml"
	"strings"
	"text/template"
)

var stdinReader converters.InputReader = converters.StdinReader{}
var serializer converters.Serializer = converters.ProtobufSerializer{}

func main() {
	path, rawTemplate, rawValues, processingError := prepareData()

	evaluatedTemplate := ""
	if processingError == nil {
		fmt.Fprintf(os.Stderr, "Read in total %d characters from stdin; evaluating template <%s>\n", len(rawTemplate)+len(rawValues), path)
		evaluatedTemplate, processingError = evaluateTemplateInternal(path, rawTemplate, rawValues)
	} else {
		fmt.Fprintf(os.Stderr, "Failed to read input: %s\n", processingError.Error())
	}

	result, err := serializer.Serialize(evaluatedTemplate, processingError)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to serialize evaluated template to Protobuf: %s\n", err.Error())
		os.Exit(1)
	}
	fmt.Fprintf(os.Stderr, "Writing %d bytes to stdout\n", len(result))
	os.Stdout.Write(result)
}

func prepareData() (string, string, string, error) {
	scanner := bufio.NewScanner(os.Stdin)
	contents, err := stdinReader.ReadInput(scanner)
	if err != nil {
		return "", "", "", fmt.Errorf("error reading content: %w", err)
	}
	if err = validateContents(contents); err != nil {
		return "", "", "", fmt.Errorf("error validating content: %w", err)
	}

	path := contents[0].Name
	rawTemplate := contents[0].Content
	rawValues := contents[1].Content
	return path, rawTemplate, rawValues, nil
}

func validateContents(contents []converters.Content) error {
	if len(contents) == 0 {
		return errors.New("no input received")
	} else if len(contents) != 2 {
		return fmt.Errorf("expected 2 files, received %d files, possible missing values file", len(contents))
	}
	return nil
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
		return "", fmt.Errorf("error parsing values file: %w", err)
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
