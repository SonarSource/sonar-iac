// SonarQube IaC Plugin
// Copyright (C) 2021-2025 SonarSource SA
// mailto:info AT sonarsource DOT com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the Sonar Source-Available License for more details.
//
// You should have received a copy of the Sonar Source-Available License
// along with this program; if not, see https://sonarsource.com/license/ssal/

package main

import (
	"fmt"
	"github.com/SonarSource/sonar-iac/sonar-helm-for-iac/src/converters"
	pbstructs "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/src/org.sonar.iac.helm"
	"github.com/sonarsource/go/src/text/template"
	"github.com/sonarsource/go/src/text/template/parse"
	"os"
	"strings"
)

var converter converters.Converter = &converters.DefaultConverter{}
var serializer converters.Serializer = converters.ProtobufSerializer{}
var loggingCollector = converters.NewDefaultLoggingCollector()
var sourceInput = os.Stdin

func main() {
	templateSources, processingError := converters.ReadAndValidateSources(sourceInput, &loggingCollector)

	evaluatedTemplate := ""
	var ast *pbstructs.Tree
	if processingError == nil {
		loggingCollector.AppendLog(fmt.Sprintf("Read in total %d files from stdin; evaluating template <%s>\n", templateSources.NumSources(), templateSources.Name))
		evaluationResult := evaluateTemplate(templateSources)
		ast = converter.ConvertTree(templateSources.TemplateFile(), evaluationResult.Ast)
		evaluatedTemplate, processingError = evaluationResult.Template, evaluationResult.Error
	} else {
		fmt.Fprintf(os.Stderr, "Failed to read input: %s\n", processingError.Error())
	}

	if processingError != nil {
		loggingCollector.FlushLogs()
	}

	result, err := serializer.Serialize(evaluatedTemplate, ast, processingError)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to serialize evaluated template to Protobuf: %s\n", err.Error())
		os.Exit(1)
	}
	os.Stdout.Write(result)
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

	return executePreparedTemplate(tmpl, templateName, values)
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
