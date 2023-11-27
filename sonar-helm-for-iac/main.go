package main

import "C"
import (
	"fmt"
	"strings"
	"text/template"

	"sigs.k8s.io/yaml"
)

func main() {
	fmt.Println("Hello World!")
}

var handles []*template.Template

//export EvaluateTemplate
func EvaluateTemplate(path string, content string, valuesFileContent string) *C.char {
	return C.CString(evaluateTemplateInternal(path, content, valuesFileContent))
}

// For tests, the C code doesn't work in tests
func evaluateTemplateInternal(path string, content string, valuesFileContent string) string {
	templateId := newHandleID(path, content)
	return executeWithValues(templateId, valuesFileContent)
}

// Create a template with name and expression and return its handle (a numeric ID to access the template later)
func newHandleID(name string, content string) (rc int) {
	defer func() {
		if err := recover(); err != nil {
			fmt.Println("panic occurred: ", err)
		}
	}()

	t := template.New(name)
	t.Funcs(*addCustomFunctions())
	t, err := t.Parse(content)
	if err != nil {
		fmt.Println("Error parsing template: ", err)
		return -1
	}

	handles = append(handles, t)
	return len(handles) - 1
}

func executeWithValues(templateId int, valuesFilePath string) string {
	valuesMap, err := yamlToMap(valuesFilePath)
	if err != nil {
		//TODO return error to Java?
		fmt.Println("Error reading values file: ", err)
		return ""
	}
	vals := struct {
		Values map[string]interface{}
	}{valuesMap}

	tmpl := handles[templateId]
	var buf strings.Builder
	err = tmpl.Execute(&buf, vals)
	if err != nil {
		fmt.Println("Error executing template: ", err)
		return ""
	}
	return buf.String()
}

func yamlToMap(input string) (map[string]interface{}, error) {
	vals := map[string]interface{}{}
	err := yaml.Unmarshal([]byte(input), &vals)
	if len(vals) == 0 {
		vals = map[string]interface{}{}
	}
	return vals, err
}
