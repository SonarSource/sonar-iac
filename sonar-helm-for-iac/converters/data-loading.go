package converters

import (
	"fmt"
	"github.com/samber/mo"
	"sigs.k8s.io/yaml"
)

// TemplateSources contains all the sources needed to evaluate a template
type TemplateSources struct {
	Name  string
	files Files
}

func NewTemplateSources(name string, fileNameToFileContent Files) *TemplateSources {
	return &TemplateSources{name, fileNameToFileContent}
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

	values := mo.TupleToResult(LoadValues(templateSources.Values()))
	chart, err := FlatMap(values, func(values *Values) mo.Result[*Chart] {
		return Map(mo.TupleToResult(templateSources.SourceFile("Chart.yaml")), func(content []byte) (*Chart, error) {
			return LoadChart(string(content))
		})
	}).Get()

	if err != nil {
		return nil, err
	}

	result["Values"] = *values.MustGet()
	result["Chart"] = *chart
	result["Files"] = templateSources.files

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

func FlatMap[T any, R any](result mo.Result[T], mapper func(T) mo.Result[R]) mo.Result[R] {
	if result.IsOk() {
		return mapper(result.MustGet())
	}
	return mo.Err[R](result.Error())
}
