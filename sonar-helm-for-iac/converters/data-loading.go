package converters

import (
	"fmt"
	"github.com/samber/mo"
	"sigs.k8s.io/yaml"
)

// TemplateSources contains all the sources needed to evaluate a template
type TemplateSources struct {
	Name                  string
	RawTemplate           string
	fileNameToFileContent map[string]string
}

func NewTemplateSources(name string, rawTemplate string, fileNameToFileContent map[string]string) *TemplateSources {
	return &TemplateSources{name, rawTemplate, fileNameToFileContent}
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

func PrepareChartValues(templateSources *TemplateSources) (map[string]interface{}, error) {
	result := map[string]interface{}{}

	values := mo.TupleToResult(LoadValues(templateSources.Values()))
	chart, err := Map(values, func(values *Values) (*Chart, error) {
		return Map(mo.TupleToResult(templateSources.SourceFile("Chart.yaml")), func(content string) (*Chart, error) {
			return LoadChart(content)
		}).Get()
	}).Get()

	if err != nil {
		return nil, err
	}

	result["Values"] = values.MustGet()
	result["Chart"] = chart

	result["Capabilities"] = DefaultCapabilities
	result["Release"] = DefaultReleaseMetadata
	result["Template"] = Template{
		Name:     templateSources.Name,
		BasePath: getBasePath(chart),
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
	return &chartMap, nil
}

func getBasePath(chart *Chart) string {
	// TODO SONARIAC-1241: handle nested charts, i.e. basePath should incorporate parent path like "parent-chart/charts/nested-chart/templates"
	return (*chart)["name"].(string) + "/templates"
}

func unmarshalYamlToMap(input string) (map[string]interface{}, error) {
	values := map[string]interface{}{}
	err := yaml.Unmarshal([]byte(input), &values)
	if len(values) == 0 {
		values = map[string]interface{}{}
	}
	return values, err
}

func Map[T any, R any](result mo.Result[T], mapper func(T) (R, error)) mo.Result[R] {
	if result.IsOk() {
		return mo.TupleToResult(mapper(result.MustGet()))
	}
	return mo.Err[R](result.Error())
}
