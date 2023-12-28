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

// TemplateSources contains all the sources needed to evaluate a template
type TemplateSources struct {
	Name                  string
	RawTemplate           string
	fileNameToFileContent map[string]string
}

// NumAdditionalSources returns the number of sources required for this template.
func (ts *TemplateSources) NumAdditionalSources() int {
	return len(ts.fileNameToFileContent)
}

func (ts *TemplateSources) SourceFile(name string) (string, error) {
	if _, ok := ts.fileNameToFileContent[name]; !ok {
		return "", fmt.Errorf("source file %s not found", name)
	}
	return ts.fileNameToFileContent[name], nil
}

func (ts *TemplateSources) Values() string {
	valuesFile, _ := ts.SourceFile("values.yaml")
	return valuesFile
}

func NewTemplateSourcesFromRawSources(rawSources []converters.SourceCode) *TemplateSources {
	sources := make(map[string]string)
	// The first file is assumed to be the main template, the rest are additional files
	for _, source := range rawSources[1:] {
		sources[source.Name] = source.Content
	}
	return &TemplateSources{rawSources[0].Name, rawSources[0].Content, sources}
}

func main() {
	templateSources, processingError := readAndValidateSources()

	evaluatedTemplate := ""
	if processingError == nil {
		fmt.Fprintf(os.Stderr, "Read in total %d files from stdin; evaluating template <%s>\n", templateSources.NumAdditionalSources(), templateSources.Name)
		evaluatedTemplate, processingError = evaluateTemplate(templateSources)
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

func readAndValidateSources() (*TemplateSources, error) {
	scanner := bufio.NewScanner(os.Stdin)
	sources, err := stdinReader.ReadInput(scanner)
	if err != nil {
		return nil, fmt.Errorf("error reading content: %w", err)
	}
	if err = validateInput(sources); err != nil {
		return nil, fmt.Errorf("error validating content: %w", err)
	}

	return NewTemplateSourcesFromRawSources(sources), nil
}

func validateInput(sources []converters.SourceCode) error {
	if len(sources) == 0 {
		return errors.New("no input received")
	}
	return nil
}

func evaluateTemplate(templateSources *TemplateSources) (string, error) {
	tmpl, err := newTemplate(templateSources.Name, templateSources.RawTemplate)
	if err != nil {
		return "", err
	}
	return executeWithValues(tmpl, templateSources.Values())
}

func newTemplate(name string, content string) (*template.Template, error) {
	tmpl := template.New(name)
	tmpl.Funcs(*addCustomFunctions())
	tmpl, err := tmpl.Parse(content)
	if err != nil {
		return nil, err
	}

	return tmpl, nil
}

func executeWithValues(tmpl *template.Template, valuesFileContent string) (string, error) {
	valuesMap, err := unmarshalYamlToMap(valuesFileContent)
	if err != nil {
		return "", fmt.Errorf("error parsing values file: %w", err)
	}
	values := struct {
		Values map[string]interface{}
	}{valuesMap}

	var buf strings.Builder
	err = tmpl.Execute(&buf, values)
	if err != nil {
		return "", err
	}
	return buf.String(), nil
}

func unmarshalYamlToMap(input string) (map[string]interface{}, error) {
	values := map[string]interface{}{}
	err := yaml.Unmarshal([]byte(input), &values)
	if len(values) == 0 {
		values = map[string]interface{}{}
	}
	return values, err
}
