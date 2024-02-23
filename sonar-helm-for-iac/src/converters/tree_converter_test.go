package converters

import (
	pbstructs "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/src/org.sonar.iac.helm"
	"github.com/sonarsource/template"
	"github.com/stretchr/testify/assert"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/types/known/anypb"
	"strings"
	"testing"
)

type TestConverter struct {
	DefaultConverter
}

var converter = &TestConverter{}

func Test_ignore_nil(t *testing.T) {
	tree := converter.ConvertTree("", nil)
	assert.Nil(t, tree)
}

func Test_convert_simple(t *testing.T) {
	code := "{{ . }}"
	//       ^^^^^^^ List
	//          ^    Action -> Pipe -> Command -> Dot

	tpl, _ := template.New("test").Parse(code)
	ctx := ConversionContext{
		Content:   code,
		Converter: converter,
	}
	node := ctx.Convert(tpl.Root).(*pbstructs.ListNode)

	assert.Equal(t, pbstructs.NodeType_NodeList, node.NodeType)
	assert.Equal(t, 1, len(node.Nodes))
	assert.Equal(t, int64(0), node.Pos)
	assert.Equal(t, int64(7), node.Length)
	n, _ := anypb.UnmarshalNew(node.Nodes[0], proto.UnmarshalOptions{})
	assert.True(t, strings.HasSuffix(node.Nodes[0].TypeUrl, "ActionNode"))
	actionNode := n.(*pbstructs.ActionNode)
	assert.Equal(t, int64(3), actionNode.Pos)
	assert.Equal(t, int64(1), actionNode.Length)
	assert.Equal(t, pbstructs.NodeType_NodePipe, actionNode.Pipe.NodeType)
	assert.Nil(t, actionNode.Pipe.Decl)
	cmd := actionNode.Pipe.Cmds[0]
	assert.Equal(t, int64(3), cmd.Pos)
	assert.Equal(t, int64(1), cmd.Length)
	assert.Equal(t, pbstructs.NodeType_NodeCommand, cmd.NodeType)
	n, _ = anypb.UnmarshalNew(cmd.Args[0], proto.UnmarshalOptions{})
	assert.True(t, strings.HasSuffix(cmd.Args[0].TypeUrl, "DotNode"))
	arg := n.(*pbstructs.DotNode)
	assert.Equal(t, pbstructs.NodeType_NodeDot, arg.NodeType)
	assert.Equal(t, int64(3), arg.Pos)
	assert.Equal(t, int64(1), arg.Length)
}

func Test_convert_comment(t *testing.T) {
	code := "{{/* comment */}}"
	//         ^^^^^^^^^^^^^^^ List (opening braces are not included)
	//         ^^^^^^^^^^^^^   Comment

	tpl, _ := template.New("test").Parse(code)
	ctx := ConversionContext{
		Content:   code,
		Converter: converter,
	}
	node := ctx.Convert(tpl.Root).(*pbstructs.ListNode)
	assert.Equal(t, 1, len(node.Nodes))
	// TODO: Pos is 2 and not 0, because `lex.go:lexLeftDelim` ignores opening braces if followed by `/*`.
	//  These characters are not included in the length as well.
	assert.Equal(t, int64(2), node.Pos)
	assert.Equal(t, int64(15), node.Length)
	n, _ := anypb.UnmarshalNew(node.Nodes[0], proto.UnmarshalOptions{})
	assert.True(t, strings.HasSuffix(node.Nodes[0].TypeUrl, "CommentNode"))
	commentNode := n.(*pbstructs.CommentNode)
	assert.Equal(t, "/* comment */", *commentNode.Text)
	assert.Equal(t, int64(2), commentNode.Pos)
	assert.Equal(t, int64(13), commentNode.Length)
}

func Test_TreeConvert_simple_dot(t *testing.T) {
	code := "{{ . }}"
	tpl, _ := template.New("test-simple-dot").Parse(code)

	tree := converter.ConvertTree(code, tpl.Tree)

	basicTreeAsserts(t, tree, "test-simple-dot", 7, 0, 1)
}

func nodeFromAction(code string) (*pbstructs.Tree, proto.Message) {
	tpl, _ := template.New("test").Parse(code)
	tree := converter.ConvertTree(code, tpl.Tree)

	n, _ := anypb.UnmarshalNew(tree.Root.Nodes[0], proto.UnmarshalOptions{})
	actionNode := n.(*pbstructs.ActionNode)
	pipe := actionNode.Pipe
	arg, _ := anypb.UnmarshalNew(pipe.Cmds[0].Args[0], proto.UnmarshalOptions{})
	return tree, arg
}

func Test_TestNil(t *testing.T) {
	tree, arg := nodeFromAction(`{{ nil }}`)

	basicTreeAsserts(t, tree, "test", 9, 0, 1)
	nilNode := arg.(*pbstructs.NilNode)
	assert.Equal(t, int64(3), nilNode.Pos)
	assert.Equal(t, int64(3), nilNode.Length)
}

func Test_TestBool(t *testing.T) {
	tree, arg := nodeFromAction(`{{ true }}`)

	basicTreeAsserts(t, tree, "test", 10, 0, 1)
	boolNode := arg.(*pbstructs.BoolNode)
	assert.Equal(t, int64(3), boolNode.Pos)
	assert.Equal(t, int64(4), boolNode.Length)
}

func Test_TestNumber(t *testing.T) {
	tree, arg := nodeFromAction(`{{ 42.5 }}`)

	basicTreeAsserts(t, tree, "test", 10, 0, 1)
	numberNode := arg.(*pbstructs.NumberNode)
	assert.Equal(t, int64(3), numberNode.Pos)
	assert.Equal(t, int64(4), numberNode.Length)
}

func Test_TestString(t *testing.T) {
	tree, arg := nodeFromAction(`{{ "foo" }}`)

	basicTreeAsserts(t, tree, "test", 11, 0, 1)
	nilNode := arg.(*pbstructs.StringNode)
	assert.Equal(t, int64(3), nilNode.Pos)
	assert.Equal(t, int64(5), nilNode.Length)
}

func Test_TestContinueBreak(t *testing.T) {
	code := `{{ range .Values.annotations }}{{ continue }}{{ break }}{{ end }}`
	//       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ List (0; 65)
	//                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^    Range (9; 53)
	//                ^^^^^^^^^^^^^^^^^^^^                                     Pipe (9; 20)
	//                                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^    List (31; 31)
	//                                         ^^^^^^^^                        Continue (34; 8)
	//                                                       ^^^^^             Break (48; 5)
	tpl, _ := template.New("test-continue").Parse(code)

	tree := converter.ConvertTree(code, tpl.Tree)

	basicTreeAsserts(t, tree, "test-continue", 65, 0, 1)
	n, _ := anypb.UnmarshalNew(tree.Root.Nodes[0], proto.UnmarshalOptions{})
	rangeNode := n.(*pbstructs.RangeNode)
	// TODO: range doesn't include the `range` keyword
	assert.Equal(t, int64(9), rangeNode.Pos)
	// TODO: nodeEnd doesn't include closing braces, so length stops after `end` keyword
	assert.Equal(t, int64(53), rangeNode.Length)

	pipe := rangeNode.BranchNode.Pipe
	// pipe starts after the range keyword
	assert.Equal(t, int64(9), pipe.Pos)
	assert.Equal(t, int64(20), pipe.Length)

	list := rangeNode.BranchNode.List
	assert.Equal(t, int64(31), list.Pos)
	// list ends after `end` keyword before closing braces
	assert.Equal(t, int64(31), list.Length)

	n, _ = anypb.UnmarshalNew(rangeNode.BranchNode.List.Nodes[0], proto.UnmarshalOptions{})
	continueNode := n.(*pbstructs.ContinueNode)
	// ContinueNode doesn't include braces and spaces
	assert.Equal(t, int64(34), continueNode.Pos)
	assert.Equal(t, int64(8), continueNode.Length)

	n, _ = anypb.UnmarshalNew(rangeNode.BranchNode.List.Nodes[1], proto.UnmarshalOptions{})
	breakNode := n.(*pbstructs.BreakNode)
	// BreakNode doesn't include braces and spaces
	assert.Equal(t, int64(48), breakNode.Pos)
	assert.Equal(t, int64(5), breakNode.Length)

	assert.Nil(t, rangeNode.BranchNode.ElseList)
}

func Test_TestChain(t *testing.T) {
	tree, arg := nodeFromAction(`{{ (.Values).foo.bar }}`)

	basicTreeAsserts(t, tree, "test", 23, 0, 1)
	chainNode := arg.(*pbstructs.ChainNode)
	// starts after parentheses
	assert.Equal(t, int64(12), chainNode.Pos)
	assert.Equal(t, int64(8), chainNode.Length)
	assert.Equal(t, 2, len(chainNode.Field))
	n, _ := anypb.UnmarshalNew(chainNode.Node, proto.UnmarshalOptions{})
	pipeNode := n.(*pbstructs.PipeNode)
	// parentheses are not included in the pipe
	assert.Equal(t, int64(4), pipeNode.Pos)
	assert.Equal(t, int64(7), pipeNode.Length)
	n, _ = anypb.UnmarshalNew(pipeNode.Cmds[0].Args[0], proto.UnmarshalOptions{})
	fieldNode := n.(*pbstructs.FieldNode)
	assert.Equal(t, []string{"Values"}, fieldNode.Ident)
}

func Test_TreeConvert_define_and_template(t *testing.T) {
	code := `
{{- /* returns "foo" */ -}}
{{- define "foo" }}
{{ slice 2 "00foo" }}
{{ end }}

{{ template "foo" }}
`
	tpl, _ := template.New("test-define-and-template").Parse(code)

	tree := converter.ConvertTree(code, tpl.Tree)

	basicTreeAsserts(t, tree, "test-define-and-template", 98, 5, 4)

	n, _ := anypb.UnmarshalNew(tree.Root.Nodes[0], proto.UnmarshalOptions{})
	commentNode := n.(*pbstructs.CommentNode)
	assert.Equal(t, int64(5), commentNode.Pos)

	// Note: `define` is not present in the AST

	n, _ = anypb.UnmarshalNew(tree.Root.Nodes[1], proto.UnmarshalOptions{})
	textNode := n.(*pbstructs.TextNode)
	assert.Equal(t, int64(80), textNode.Pos)

	n, _ = anypb.UnmarshalNew(tree.Root.Nodes[2], proto.UnmarshalOptions{})
	templateNode := n.(*pbstructs.TemplateNode)
	assert.Equal(t, int64(94), templateNode.Pos)
	assert.Equal(t, "foo", *templateNode.Name)
	assert.Nil(t, templateNode.Pipe)

	n, _ = anypb.UnmarshalNew(tree.Root.Nodes[3], proto.UnmarshalOptions{})
	textNode = n.(*pbstructs.TextNode)
	assert.Equal(t, int64(102), textNode.Pos)
	assert.Equal(t, int64(1), textNode.Length)
}

func Test_TreeConvert_range(t *testing.T) {
	code := `
{{- range $key, $value := .Values.annotations }}
  {{ $key }}: {{ $value }}
{{- end }}`
	tpl, _ := template.New("test-range").Parse(code)

	tree := converter.ConvertTree(code, tpl.Tree)

	basicTreeAsserts(t, tree, "test-range", 86, 1, 1)
	n, _ := anypb.UnmarshalNew(tree.Root.Nodes[0], proto.UnmarshalOptions{})
	rangeNode := n.(*pbstructs.RangeNode)
	assert.Equal(t, int64(11), rangeNode.Pos)
	// From `$key` to the end of the `end` keyword
	assert.Equal(t, int64(73), rangeNode.Length)
	assert.NotNil(t, rangeNode.BranchNode.Pipe)
	assert.NotNil(t, rangeNode.BranchNode.List)
	assert.Nil(t, rangeNode.BranchNode.ElseList)

	n, _ = anypb.UnmarshalNew(rangeNode.BranchNode.Pipe.Cmds[0].Args[0], proto.UnmarshalOptions{})
	fieldNode := n.(*pbstructs.FieldNode)
	assert.Equal(t, int64(34), fieldNode.Pos)
	assert.Equal(t, int64(12), fieldNode.Length)

	list := rangeNode.BranchNode.List
	assert.Equal(t, 4, len(list.Nodes))
	assert.True(t, strings.HasSuffix(list.Nodes[0].TypeUrl, "TextNode"))
	assert.True(t, strings.HasSuffix(list.Nodes[1].TypeUrl, "ActionNode"))
	assert.True(t, strings.HasSuffix(list.Nodes[2].TypeUrl, "TextNode"))
	assert.True(t, strings.HasSuffix(list.Nodes[3].TypeUrl, "ActionNode"))
}

func Test_TreeConvert(t *testing.T) {
	tests := []struct {
		name       string
		tmpl       string
		rootLength int
		rootPos    int
		numNodes   int
	}{
		{
			name: "Test_TreeConvert_range",
			tmpl: `
apiVersion: v1
kind: ConfigMap
metadata:
  {{- if .Values.annotations }}
  annotations:
  {{- range $key, $value := .Values.annotations }}
    {{ $key }}: {{ $value }}
  {{- end }}
  {{- end }}
`,
			rootLength: 195,
			rootPos:    0,
			numNodes:   3,
		},
		{
			name: "Test_TreeConvert_control_flow",
			tmpl: `
{{- if .Values.annotations }}
{{- range $key, $value := .Values.annotations }}
{{- if eq $key "foo" }} {{- break -}} {{else}} {{continue}} {{- end }}
{{ $key }}: {{ $value }}
{{- end }}
{{- else if false }}
{{- printf nil }}
{{- printf 2.0 }}
{{- else }}
{{ print .Values.fallbacks.foo }}
{{- end }}
`,
			rootLength: 300,
			rootPos:    1,
			numNodes:   2,
		},
		{
			name: "Test_TreeConvert_with",
			tmpl: `
{{- with .Values.annotations }}
{{- range $key, $value := . }}
{{ $key }}: {{ $value }}
{{- end }}
{{- end }}
`,
			rootLength: 110,
			rootPos:    1,
			numNodes:   2,
		},
		{
			name: "Test_TreeConvert_list_single_pipe",
			tmpl: `
{{- len ( print .Values.foo) }}
`,
			rootLength: 32,
			rootPos:    1,
			numNodes:   2,
		},
		{
			name:       "Test_TreeConvert_ChainNode",
			tmpl:       `{{ print (.Values).escalation }}`,
			rootLength: 32,
			rootPos:    0,
			numNodes:   1,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			tpl, err := template.New(tt.name).Parse(tt.tmpl)
			assert.NoError(t, err)

			tree := converter.ConvertTree(tt.tmpl, tpl.Tree)

			basicTreeAsserts(t, tree, tt.name, tt.rootLength, tt.rootPos, tt.numNodes)
		})
	}
}

func basicTreeAsserts(t *testing.T, tree *pbstructs.Tree, name string, rootLength int, rootPos int, rootNumNodes int) {
	assert.NotNil(t, tree)
	assert.Equal(t, name, *tree.Name, "Tree name")
	assert.Equal(t, name, *tree.ParseName, "Tree parse name")
	assert.Equal(t, int64(rootLength), tree.Root.Length, "Length of root node")
	assert.Equal(t, int64(rootPos), tree.Root.Pos, "StartOffset of root node")
	assert.Equal(t, rootNumNodes, len(tree.Root.Nodes), "Number of nodes in root")
}
