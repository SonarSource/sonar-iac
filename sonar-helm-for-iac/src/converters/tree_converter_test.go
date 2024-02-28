package converters

import (
	pbstructs "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/src/org.sonar.iac.helm"
	"github.com/stretchr/testify/assert"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/types/known/anypb"
	"strings"
	"testing"
	"text/template"
	"text/template/parse"
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
	tpl, _ := template.New("test").Parse(code)
	ctx := ConversionContext{
		Content:   code,
		Converter: converter,
	}
	node := ctx.Convert(tpl.Root).(*pbstructs.ListNode)

	assert.Equal(t, pbstructs.NodeType_NodeList, node.NodeType)
	assert.Equal(t, 1, len(node.Nodes))
	n1, _ := anypb.UnmarshalNew(node.Nodes[0], proto.UnmarshalOptions{})
	assert.True(t, strings.HasSuffix(node.Nodes[0].TypeUrl, "ActionNode"))
	actionNode := n1.(*pbstructs.ActionNode)
	assert.Equal(t, pbstructs.NodeType_NodePipe, actionNode.Pipe.NodeType)
	assert.Nil(t, actionNode.Pipe.Decl)
	cmd := actionNode.Pipe.Cmds[0]
	assert.Equal(t, pbstructs.NodeType_NodeCommand, cmd.NodeType)
	a1, _ := anypb.UnmarshalNew(cmd.Args[0], proto.UnmarshalOptions{})
	assert.True(t, strings.HasSuffix(cmd.Args[0].TypeUrl, "DotNode"))
	arg := a1.(*pbstructs.DotNode)
	assert.Equal(t, pbstructs.NodeType_NodeDot, arg.NodeType)
}

// By default, `text/template` does not add comments into the AST. To change this, `text/template.Tree` has a `Mode` field
// which can be set to `parse.ParseComments`. It can't be done from `text/template` directly, only in `text/template/parse`.
// This test describes the existing behavior until we need to change it.
func Test_comments_are_ignored(t *testing.T) {
	code := "{{/* comment */}}"
	tpl, _ := template.New("test").Parse(code)
	ctx := ConversionContext{
		Content:   code,
		Converter: converter,
	}
	node := ctx.Convert(tpl.Root).(*pbstructs.ListNode)
	assert.Equal(t, 0, len(node.Nodes))
}

// This test shows how to enable comments in the AST.
func Test_convert_comments(t *testing.T) {
	code := "{{/* comment */}}"
	tree := parse.New("test")
	tree.Mode = parse.ParseComments
	tree, _ = tree.Parse(code, "", "", make(map[string]*parse.Tree))
	ctx := ConversionContext{
		Content:   code,
		Converter: converter,
	}

	node := ctx.Convert(tree.Root).(*pbstructs.ListNode)

	assert.Equal(t, 1, len(node.Nodes))
	n1, _ := anypb.UnmarshalNew(node.Nodes[0], proto.UnmarshalOptions{})
	assert.True(t, strings.HasSuffix(node.Nodes[0].TypeUrl, "CommentNode"))
	commentNode := n1.(*pbstructs.CommentNode)
	assert.Equal(t, "/* comment */", *commentNode.Text)
}

func Test_TreeConvert_simple_dot(t *testing.T) {
	code := "{{ . }}"
	tpl, _ := template.New("test-simple-dot").Parse(code)

	tree := converter.ConvertTree(code, tpl.Tree)

	basicTreeAsserts(t, tree, "test-simple-dot", 5, 0, 1)
	n, _ := anypb.UnmarshalNew(tree.Root.Nodes[0], proto.UnmarshalOptions{})
	actionNode := n.(*pbstructs.ActionNode)
	assert.Equal(t, int64(3), actionNode.Pos)
	assert.Equal(t, int64(5), actionNode.Length)
	assert.Equal(t, int64(1), actionNode.Pipe.Length)
	assert.Empty(t, actionNode.Pipe.Decl)
	assert.Equal(t, 1, len(actionNode.Pipe.Cmds))
	cmd := actionNode.Pipe.Cmds[0]
	assert.Equal(t, int64(1), cmd.Length)
	assert.Equal(t, 1, len(cmd.Args))
	n, _ = anypb.UnmarshalNew(cmd.Args[0], proto.UnmarshalOptions{})
	dot := n.(*pbstructs.DotNode)
	assert.Equal(t, int64(3), dot.Pos)
	assert.Equal(t, int64(1), dot.Length)
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

	basicTreeAsserts(t, tree, "test-define-and-template", 21, 29, 3)

	n, _ := anypb.UnmarshalNew(tree.Root.Nodes[0], proto.UnmarshalOptions{})
	textNode := n.(*pbstructs.TextNode)
	// Note: comment and `define` nodes are not present in the AST
	assert.Equal(t, int64(80), textNode.Pos)

	n, _ = anypb.UnmarshalNew(tree.Root.Nodes[1], proto.UnmarshalOptions{})
	templateNode := n.(*pbstructs.TemplateNode)
	assert.Equal(t, int64(94), templateNode.Pos)
	assert.Equal(t, "foo", *templateNode.Name)
	assert.Nil(t, templateNode.Pipe)

	n, _ = anypb.UnmarshalNew(tree.Root.Nodes[2], proto.UnmarshalOptions{})
	textNode = n.(*pbstructs.TextNode)
	assert.Equal(t, int64(102), textNode.Pos)
}

func Test_TreeConvert_range(t *testing.T) {
	code := `
{{- range $key, $value := .Values.annotations }}
  {{ $key }}: {{ $value }}
{{- end }}`
	tpl, _ := template.New("test-range").Parse(code)

	tree := converter.ConvertTree(code, tpl.Tree)

	basicTreeAsserts(t, tree, "test-range", 75, 1, 1)
	n, _ := anypb.UnmarshalNew(tree.Root.Nodes[0], proto.UnmarshalOptions{})
	rangeNode := n.(*pbstructs.RangeNode)
	assert.Equal(t, int64(11), rangeNode.Pos)
	assert.Equal(t, int64(75), rangeNode.Length)
	assert.NotNil(t, rangeNode.BranchNode.Pipe)
	assert.NotNil(t, rangeNode.BranchNode.List)
	assert.Nil(t, rangeNode.BranchNode.ElseList)

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
			rootLength: 167,
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
			rootLength: 259,
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
			rootLength: 91,
			rootPos:    1,
			numNodes:   2,
		},
		{
			name: "Test_TreeConvert_list_single_pipe",
			tmpl: `
{{- len ( print .Values.foo) }}
`,
			rootLength: 28,
			rootPos:    1,
			numNodes:   2,
		},
		{
			name:       "Test_test",
			tmpl:       `{{ print (.Values).escalation }}`,
			rootLength: 30,
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
	assert.Equal(t, int64(rootPos), tree.Root.Pos, "Position of root node")
	assert.Equal(t, rootNumNodes, len(tree.Root.Nodes), "Number of nodes in root")
}
