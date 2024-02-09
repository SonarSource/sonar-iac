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
	pbstructs "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/org.sonar.iac.helm"
	"os"
	"strings"
	"text/template"
	"text/template/parse"
)

var stdinReader converters.InputReader = converters.StdinReader{}
var converter converters.Converter = &converters.DefaultConverter{}
var serializer converters.Serializer = converters.ProtobufSerializer{}

func NewTemplateSourcesFromRawSources(templateName string, rawSources converters.Files) *converters.TemplateSources {
	return converters.NewTemplateSources(templateName, rawSources)
}

func main() {
	templateSources, processingError := readAndValidateSources()

	evaluatedTemplate := ""
	var ast *pbstructs.Tree
	if processingError == nil {
		fmt.Fprintf(os.Stderr, "Read in total %d files from stdin; evaluating template <%s>\n", templateSources.NumSources(), templateSources.Name)
		evaluationResult := evaluateTemplate(templateSources)
		ast = converter.ConvertTree(templateSources.TemplateFile(), evaluationResult.Ast)
		evaluatedTemplate, processingError = evaluationResult.Template, evaluationResult.Error
	} else {
		fmt.Fprintf(os.Stderr, "Failed to read input: %s\n", processingError.Error())
	}

	result, err := serializer.Serialize(evaluatedTemplate, ast, processingError)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to serialize evaluated template to Protobuf: %s\n", err.Error())
		os.Exit(1)
	}
	fmt.Fprintf(os.Stderr, "Writing %d bytes to stdout\n", len(result))
	os.Stdout.Write(result)
}

func readAndValidateSources() (*converters.TemplateSources, error) {
	scanner := bufio.NewScanner(os.Stdin)
	templateName, sources, err := stdinReader.ReadInput(scanner)
	if err != nil {
		return nil, fmt.Errorf("error reading content: %w", err)
	}
	if err = validateInput(sources); err != nil {
		return nil, fmt.Errorf("error validating content: %w", err)
	}

	return NewTemplateSourcesFromRawSources(templateName, sources), nil
}

func validateInput(sources converters.Files) error {
	if len(sources) == 0 {
		return errors.New("no input received")
	}
	return nil
}

type EvaluationResult struct {
	Template string
	Ast      *parse.Tree
	Error    error
}

func resultWithError(err error) EvaluationResult {
	return EvaluationResult{"", nil, err}
}

func evaluateTemplate(templateSources *converters.TemplateSources) EvaluationResult {
	data, err := converters.PrepareChartValues(templateSources)
	if err != nil {
		return resultWithError(err)
	}

	referenceFiles := templateSources.TemplateFiles(data["Chart"].(converters.Chart)["Name"].(string))
	return evaluateTemplateWithReferences(templateSources.Name, templateSources.TemplateFile(), data, &referenceFiles)
}

// evaluateTemplateWithReferences evaluates a template also evaluating included templates.
// Note: in Helm, similar function accepts templateName and basePath as parameters, but since we are evaluating only single file at a time
// (the file passed from Java code), we don't need to. For the same reason we can assume that template `templateName` will always be subject
// for rendering, e.g. it won't be a partial (name starting with underscore).
func evaluateTemplateWithReferences(templateName string, templateContent string, values map[string]any, referenceFiles *converters.Files) EvaluationResult {
	tmpl := template.New("aggregatingTemplate")
	// Substitute some of the missing values with zero values. Others will be additionally handled after rendering.
	tmpl.Option("missingkey=zero")

	funcMap := *addCustomFunctions(tmpl)
	funcMap["tpl"] = buildTplFunction(templateName, referenceFiles)
	tmpl.Funcs(funcMap)

	_, err := tmpl.New(templateName).Parse(templateContent)
	if err != nil {
		return resultWithError(err)
	}

	err = addTemplatesIfMissing(tmpl, referenceFiles)
	if err != nil {
		return resultWithError(err)
	}

	result := executePreparedTemplate(tmpl, templateName, values)
	return result
}

func addTemplatesIfMissing(tmpl *template.Template, referenceFiles *converters.Files) error {
	for name, content := range *referenceFiles {
		if tmpl.Lookup(name) == nil {
			_, err := tmpl.New(name).Parse(string(content))
			if err != nil {
				return err
			}
		}
	}
	return nil
}

func executePreparedTemplate(tmpl *template.Template, templateName string, values map[string]any) EvaluationResult {
	var buf strings.Builder
	err := tmpl.ExecuteTemplate(&buf, templateName, values)
	if err != nil {
		return resultWithError(err)
	}
	// Helm allows some unresolvable (i.e. not provided) values, but Go replaces them with token `<no value>`, which Helm then removes.
	result := strings.ReplaceAll(buf.String(), "<no value>", "")
	return EvaluationResult{result, tmpl.Lookup(templateName).Tree, nil}
}

func buildTplFunction(templateName string, referenceFiles *converters.Files) func(templateContent string, values converters.Values) (string, error) {
	return func(templateContent string, values converters.Values) (string, error) {
		evaluationResult := evaluateTemplateWithReferences(templateName, templateContent, values, referenceFiles)
		err := evaluationResult.Error

		if err != nil {
			return "", err
		}
		return evaluationResult.Template, nil
	}
}
