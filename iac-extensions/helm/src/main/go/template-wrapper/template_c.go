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
	"sync"
	"text/template"
	"text/template/parse"

  "unsafe"
	"os"
	"reflect"
	org_sonarsource_iac_helm "sonarsource/golang-template/org.sonarsource.iac.helm"

	"github.com/Masterminds/sprig/v3"
	"sigs.k8s.io/yaml"
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

//export PrintTree
func PrintTree(templateId int) *C.char {
  t := handles[templateId]
  return C.CString(printTree(t.Tree.Root, 0))
}

//export SerializeToProtobufBytes
func SerializeToProtobufBytes(templateId int64) (unsafe.Pointer, C.int) {
  t := handles[templateId]
  bytes := org_sonarsource_iac_helm.Marshal(t.Root)
  return C.CBytes(bytes), C.int(len(bytes))
}

// can be used to serialize to a file for debugging
////export SerializeToFile
// func SerializeToFile(templateId int64) {
//   t := handles[templateId]
//   bytes := org_sonarsource_iac_helm.Marshal(t.Root)
//   os.WriteFile(fmt.Sprintf("tree-%d.pb", templateId), bytes, 0644)
// }

func printTree(node parse.Node, indent int) string {
  var text string
	switch n := node.(type) {
	case nil:
		text = "nil"
	case *parse.ActionNode:
	  text = "ActionNode:\n" + printTree(n.Pipe, indent + 1)
	case *parse.CommentNode:
		text = "CommentNode:" + n.Text
	case *parse.TextNode:
	  text = fmt.Sprintf("TextNode of %d chars\n", len(n.Text))
  case *parse.ListNode:
    var buf strings.Builder
    buf.WriteString("ListNode:\n")
    for _, node := range n.Nodes {
      buf.WriteString(printTree(node, indent + 1))
    }
    text = buf.String()
  case *parse.PipeNode:
    var buf strings.Builder
    buf.WriteString("PipeNode:\n")
    for _, decl := range n.Decl {
      buf.WriteString(printTree(decl, indent + 1))
    }
    for _, cmd := range n.Cmds {
      buf.WriteString(printTree(cmd, indent + 1))
    }
    text = buf.String()
  case *parse.CommandNode:
    var buf strings.Builder
    buf.WriteString("CommandNode:\n")
    for _, arg := range n.Args {
      buf.WriteString(printTree(arg, indent + 1))
    }
    text = buf.String()
  case *parse.IdentifierNode:
    text = fmt.Sprintf("IdentifierNode: %#+v\n", n)
  case *parse.VariableNode:
    text = fmt.Sprintf("VariableNode: %#+v\n", n)
  case *parse.ChainNode:
    text = fmt.Sprintf("ChainNode: %#+v\n", n)
  case *parse.BoolNode:
    text = fmt.Sprintf("BoolNode: %#+v\n", n)
  case *parse.NumberNode:
    text = fmt.Sprintf("NumberNode: %#+v\n", n)
  case *parse.StringNode:
    text = fmt.Sprintf("StringNode: %#+v\n", n)
//  case *parse.endNode:
//    text = fmt.Sprintf("endNode: %#+v\n", n)
//  case *parse.elseNode:
//    text = fmt.Sprintf("elseNode: %#+v\n", n)
  case *parse.BranchNode:
    text = fmt.Sprintf("BranchNode: %#+v\n", n)
  case *parse.IfNode:
    text = fmt.Sprintf("IfNode: %#+v\n", n)
  case *parse.BreakNode:
    text = fmt.Sprintf("BreakNode: %#+v\n", n)
  case *parse.ContinueNode:
    text = fmt.Sprintf("ContinueNode: %#+v\n", n)
  case *parse.RangeNode:
    text = fmt.Sprintf("RangeNode: %#+v\n", n)
  case *parse.WithNode:
    text = fmt.Sprintf("WithNode: %#+v\n", n)
  case *parse.TemplateNode:
    text = fmt.Sprintf("TemplateNode: %#+v\n", n)
  case *parse.FieldNode:
    text = fmt.Sprintf("FieldNode: %#+v\n", n)
  default:
    text = fmt.Sprintf("default node: %s: %#+v %s\n", node.Type(), node, node.String())
  }
  return strings.Repeat(" ", indent) + text
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

func readValuesFile(filename string) (map[string]interface{}, error) {
	data, err := os.ReadFile(filename)
	if err != nil {
		return map[string]interface{}{}, err
	}

	vals := map[string]interface{}{}
	err = yaml.Unmarshal(data, &vals)
	if len(vals) == 0 {
	  vals = map[string]interface{}{}
	}
	return vals, err
}

//export ExecuteWithValues
func ExecuteWithValues(templateId int, valuesFilePath string) *C.char {
  valsMap, err := readValuesFile(valuesFilePath)
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
