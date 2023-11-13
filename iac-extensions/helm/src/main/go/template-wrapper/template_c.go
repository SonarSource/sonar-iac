package main

/*
typedef struct {
    char* Name;
} ExampleData;
*/
import "C"

import (
  "fmt"
  "strings"
  "text/template"
  "text/template/parse"
  "sync"
//  "unsafe"
  "reflect"
  "helm.sh/helm/v3/pkg/chartutil"
  "github.com/Masterminds/sprig/v3"
)

var handles []*template.Template

var mtx sync.Mutex

// type ExampleData struct {
//   Name string
// }

type ExampleData C.struct_ExampleData

// Create a template with name and expression and return its handle (a numeric ID to access the template later)
//export NewHandleID
func NewHandleID(name string, expression string) (rc int) {
//   mtx.Lock()
//   defer mtx.Unlock()

	defer func() {
		if err := recover(); err != nil {
			fmt.Println("panic occurred: ", err)
		}
	}()

	t, err := template.New(name).Funcs(sprig.FuncMap()).Parse(expression)
	// log error to console
	if err != nil {
	  fmt.Println("Error parsing template: ", err)
    return -1
  }

	handles = append(handles, t)
	return len(handles) - 1
}

//export GetLastTemplateNameByHandle
func GetLastTemplateNameByHandle(i int) *C.char {
	t := handles[i]
	return C.CString(t.Name())
}

//export Tree
func Tree(templateId int) {
  t := handles[templateId]
  fmt.Printf("%+v\n", t.Tree)
  fmt.Printf("%#+v\n", t.Tree.Root)
  for _, node := range t.Tree.Root.Nodes {
    fmt.Printf("%#+v\n", node)
    if (node.Type() == parse.NodeAction) {
      fmt.Printf("%#+v\n", node.(*parse.ActionNode).Pipe)
      for _, pipe := range node.(*parse.ActionNode).Pipe.Cmds {
        fmt.Printf("%#+v\n", pipe.Args)
      }
    }
  }
}

// https://pkg.go.dev/cmd/cgo#hdr-Go_references_to_C: The C type void* is represented by Go's unsafe.Pointer

// Find a template by its handle and execute it with the given data, returning the result as a string
//export Execute
func Execute(templateId int, data *C.ExampleData) *C.char {
  // convert data from C struct to Go struct
//   data = (*C.ExampleData)(unsafe.Pointer(data))

  // inspect C struct variable 'data' with Go reflection:


  fmt.Println(data.Name)

  t := reflect.TypeOf(*data)
  for i := 0; i < t.NumField(); i++ {
      ft := t.Field(i).Type
      if ft.Kind() == reflect.Ptr {
          ft = ft.Elem()
      }
      fmt.Println(ft.Kind())
  }

  tmpl := handles[templateId]
  var buf strings.Builder
  err := tmpl.Execute(&buf, *data)
  if err != nil {
    fmt.Println("Error executing template: ", err)
    return C.CString("")
  }
  return C.CString(buf.String())
}

//export ExecuteWithValues
func ExecuteWithValues(templateId int, valuesFilePath string) *C.char {
  valsMap, err := chartutil.ReadValuesFile(valuesFilePath)
  if err != nil {
    fmt.Println("Error reading values file: ", err)
    return C.CString("")
  }
  vals := struct {
    Values map[string]interface{}
  }{valsMap}
  fmt.Println("Values: ", vals)

  tmpl := handles[templateId]
  var buf strings.Builder
  err = tmpl.Execute(&buf, vals)
  if err != nil {
    fmt.Println("Error executing template: ", err)
    return C.CString("")
  }
  return C.CString(buf.String())
}

// function Execute that accepts data as any type
//export Execute
// func Execute(templateId int, data interface{}) *C.char {
  //
// }

// Unsets a template stored by its handle so it can be garbage collected by the Go runtime
//export Close
func Close(i int) {
	handles[i] = nil
}

func main() {}
