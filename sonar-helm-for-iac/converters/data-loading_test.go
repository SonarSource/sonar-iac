package converters

import (
	"github.com/samber/mo"
	"github.com/stretchr/testify/assert"
	"testing"
)

func Test_store_multiple_sources_in_memory(t *testing.T) {
	templateSources := &TemplateSources{
		Name: "test-project/templates/a.yaml",
		files: filesFromStrings(map[string]string{
			"test-project/templates/a.yaml": "apiVersion: v1",
			"values.yaml":                   "foo: bar",
			"Chart.yaml":                    "name: test-project",
		}),
	}

	assert.Equal(t, 3, templateSources.NumSources())
	assert.Equal(t, "apiVersion: v1", templateSources.TemplateFile())
	assert.Equal(t, "foo: bar", templateSources.Values())
	assert.Equal(t, "name: test-project", string(mo.TupleToResult(templateSources.SourceFile("Chart.yaml")).MustGet()))

	data, _ := PrepareChartValues(templateSources)

	assert.Contains(t, data, "Chart")
}

func Test_return_error_for_missing_file(t *testing.T) {
	templateSources := NewTemplateSources("test-project/templates/a.yaml", filesFromStrings(map[string]string{
		"test-project/templates/a.yaml": "apiVersion: v1", "values.yaml": "foo: bar"}))

	_, err := templateSources.SourceFile("Chart.yaml")

	assert.Error(t, err)
}

func Test_chart_data_has_all_fields(t *testing.T) {
	templateSources := NewTemplateSources("test-project/templates/a.yaml", filesFromStrings(map[string]string{
		"test-project/templates/a.yaml": "apiVersion: v1", "values.yaml": "foo: bar", "Chart.yaml": "name: test-project"}))

	chartData, _ := PrepareChartValues(templateSources)

	assert.Contains(t, chartData, "Values")
	assert.Equal(t, Values{"foo": "bar"}, chartData["Values"])
	assert.Contains(t, chartData, "Chart")
	assert.Equal(t, Chart{"name": "test-project"}, chartData["Chart"])
	assert.Contains(t, chartData, "Capabilities")
	assert.Equal(t, DefaultCapabilities, chartData["Capabilities"])
	assert.Contains(t, chartData, "Release")
	assert.Equal(t, DefaultReleaseMetadata, chartData["Release"])
	assert.Contains(t, chartData, "Template")
	assert.Equal(t, Template{Name: "test-project/templates/a.yaml", BasePath: "test-project/templates"}, chartData["Template"])
}

func Test_malformed_values(t *testing.T) {
	_, err := LoadValues("foo: bar: baz")

	assert.Error(t, err)
}

func Test_malformed_chart_yaml(t *testing.T) {
	_, err := LoadChart("foo: bar: baz")

	assert.Error(t, err)
}

func Test_error_when_preparing_data(t *testing.T) {
	templateSources := NewTemplateSources("test-project/templates/a.yaml", filesFromStrings(map[string]string{
		"test-project/templates/a.yaml": "apiVersion: v1", "values.yaml": "foo: bar"}))

	_, err := PrepareChartValues(templateSources)

	assert.Error(t, err)
	assert.ErrorContains(t, err, "source file Chart.yaml not found")
}

func Test_base_path(t *testing.T) {
	assert.Equal(t, "test-project/templates", getBasePath(&Chart{"name": "test-project"}))
	assert.Panics(t, func() { getBasePath(&Chart{}) })
}

func filesFromStrings(filesToStringContent map[string]string) Files {
	result := Files{}
	for name, content := range filesToStringContent {
		result[name] = []byte(content)
	}
	return result
}
