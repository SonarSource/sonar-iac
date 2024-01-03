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
	"fmt"
	"github.com/SonarSource/sonar-iac/sonar-helm-for-iac/converters"
	"os"
	"strings"
	"text/template"
)

var stdinReader converters.InputReader = converters.StdinReader{}
var serializer converters.Serializer = converters.ProtobufSerializer{}

func NewTemplateSourcesFromRawSources(rawSources []converters.SourceCode) *converters.TemplateSources {
	sources := make(map[string]string)
	// The first file is assumed to be the main template, the rest are additional files
	for _, source := range rawSources[1:] {
		sources[source.Name] = source.Content
	}
	return converters.NewTemplateSources(rawSources[0].Name, rawSources[0].Content, sources)
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

func readAndValidateSources() (*converters.TemplateSources, error) {
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

func evaluateTemplate(templateSources *converters.TemplateSources) (string, error) {
	tmpl, err := newTemplate(templateSources.Name, templateSources.RawTemplate)
	if err == nil {
		var data any
		data, err = converters.PrepareChartValues(templateSources)
		if err == nil {
			return executeWithValues(tmpl, data)
		}
	}
	return "", err
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

func executeWithValues(tmpl *template.Template, data any) (string, error) {
	var buf strings.Builder
	err := tmpl.Execute(&buf, data)
	if err != nil {
		return "", err
	}
	return buf.String(), nil
}
