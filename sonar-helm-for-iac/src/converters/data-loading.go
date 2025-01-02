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
package converters

import (
	"fmt"
	"github.com/samber/mo"
	"sigs.k8s.io/yaml"
	"strings"
	"unicode"
)

// TemplateSources contains all the sources needed to evaluate a template
type TemplateSources struct {
	Name  string
	files Files
}

func NewTemplateSources(name string, fileNameToFileContent Files) *TemplateSources {
	return &TemplateSources{
		Name:  name,
		files: fileNameToFileContent,
	}
}

// NumSources returns the number of sources required for evaluation of this template (including itself).
func (ts *TemplateSources) NumSources() int {
	return len(ts.files)
}

func (ts *TemplateSources) SourceFile(name string) ([]byte, error) {
	if _, ok := ts.files[name]; !ok {
		return nil, fmt.Errorf("source file %s not found", name)
	}
	return ts.files[name], nil
}

func (ts *TemplateSources) TemplateFile() string {
	templateFile, _ := ts.SourceFile(ts.Name)
	return string(templateFile)
}

func (ts *TemplateSources) Values() string {
	valuesFile, _ := ts.SourceFile("values.yaml")
	return string(valuesFile)
}

func PrepareChartValues(templateSources *TemplateSources) (map[string]interface{}, error) {
	result := map[string]interface{}{}

	values, err := LoadValues(templateSources.Values())

	if err != nil {
		return nil, err
	}

	chart, err := mo.Do(func() *Chart {
		content := mo.TupleToResult(templateSources.SourceFile("Chart.yaml")).MustGet()
		chart := mo.TupleToResult(LoadChart(string(content))).MustGet()
		return chart
	}).Get()

	if err != nil {
		return nil, err
	}

	result["Values"] = *values
	result["Chart"] = *chart

	result["Capabilities"] = DefaultCapabilities
	result["Release"] = DefaultReleaseMetadata

	// Helm allows referencing templates (e.g. in `template` and `include`) with kind of fully-qualified path,
	// which starts with chart name. See `pkg/chart/loader/load.go:LoadFiles`. All other files are kept intact.
	// TODO SONARIAC-1241: handle nested charts, i.e. basePath should incorporate parent path like "parent-chart/charts/nested-chart/templates"
	chartPathPrefix := (*chart)["Name"].(string)
	for filename, content := range templateSources.files {
		if strings.HasPrefix(filename, "templates/") {
			delete(templateSources.files, filename)
			templateSources.files[chartPathPrefix+"/"+filename] = content
		}
	}
	templateName := chartPathPrefix + "/" + templateSources.Name
	templateSources.Name = templateName

	result["Files"] = templateSources.files
	result["Template"] = Template{
		Name:     templateName,
		BasePath: getBasePath(templateName),
	}

	return result, err
}

func LoadValues(content string) (*Values, error) {
	valuesMap, err := unmarshalYamlToMap(content)
	if err != nil {
		return nil, fmt.Errorf("error parsing values file: %w", err)
	}
	return &valuesMap, nil
}

func LoadChart(content string) (*Chart, error) {
	chartMap, err := unmarshalYamlToMap(content)
	if err != nil {
		return nil, fmt.Errorf("error parsing Chart.yaml: %w", err)
	}
	for key, value := range chartMap {
		// Helm uses lowercase keys in Chart.yaml, but capitalized in the Metadata struct.
		delete(chartMap, key)
		if key == "apiVersion" {
			// This field should have several letters capitalized
			key = "APIVersion"
		}
		capitalizedKey := []rune(key)
		capitalizedKey[0] = unicode.ToUpper(capitalizedKey[0])
		chartMap[string(capitalizedKey)] = value
	}
	return &chartMap, nil
}

func (ts *TemplateSources) TemplateFiles(chartName string) Files {
	templateFiles := Files{}
	for filename, content := range ts.files {
		if strings.HasPrefix(filename, chartName+"/templates/") {
			templateFiles[filename] = content
		}
	}
	return templateFiles
}

func getBasePath(filepath string) string {
	basePathUntrimmed := strings.SplitAfter(filepath, "templates/")[0]
	return strings.TrimSuffix(basePathUntrimmed, "/")
}

func unmarshalYamlToMap(input string) (map[string]interface{}, error) {
	values := map[string]interface{}{}
	err := yaml.Unmarshal([]byte(input), &values)
	if len(values) == 0 {
		values = map[string]interface{}{}
	}
	return values, err
}
