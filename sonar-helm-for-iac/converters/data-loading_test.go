package converters

import (
	"github.com/samber/mo"
	"github.com/stretchr/testify/assert"
	"testing"
)

func Test_store_multiple_sources_in_memory(t *testing.T) {
	templateSources := &TemplateSources{
		Name:        "test-project/templates/a.yaml",
		RawTemplate: "apiVersion: v1",
		fileNameToFileContent: map[string]string{
			"values.yaml": "foo: bar",
			"Chart.yaml":  "name: test-project",
		},
	}

	assert.Equal(t, 2, templateSources.NumAdditionalSources())
	assert.Equal(t, "foo: bar", templateSources.Values())
	assert.Equal(t, "name: test-project", mo.TupleToResult(templateSources.SourceFile("Chart.yaml")).MustGet())

	data, _ := PrepareChartValues(templateSources)

	assert.Contains(t, data, "Chart")
}

func Test_chart_data_has_all_fields(t *testing.T) {
	templateSources := NewTemplateSources(
		"test-project/templates/a.yaml",
		"apiVersion: v1",
		map[string]string{"values.yaml": "foo: bar", "Chart.yaml": "name: test-project"})

	chartData, _ := PrepareChartValues(templateSources)

	assert.Contains(t, chartData, "Values")
	assert.Contains(t, chartData, "Chart")
	assert.Contains(t, chartData, "Capabilities")
	assert.Contains(t, chartData, "Release")
	assert.Contains(t, chartData, "Template")
}

func Test_malformed_values(t *testing.T) {
	_, err := LoadValues("foo: bar: baz")

	assert.Error(t, err)
}

func Test_malformed_chart_yaml(t *testing.T) {
	_, err := LoadChart("foo: bar: baz")

	assert.Error(t, err)
}

func Test_base_path(t *testing.T) {
	assert.Equal(t, "test-project/templates", getBasePath(&Chart{"name": "test-project"}))
	assert.Panics(t, func() { getBasePath(&Chart{}) })
}
