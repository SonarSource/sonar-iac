package converters

import (
	pbstructs "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/org.sonarsource.iac.helm"
	"github.com/stretchr/testify/assert"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/types/known/anypb"
	"strings"
	"testing"
	"text/template"
	"text/template/parse"
)

func Test_convert_simple(t *testing.T) {
	tpl, _ := template.New("test").Parse("{{ . }}")
	node := convert(tpl.Root).(*pbstructs.ListNode)

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
	tpl, _ := template.New("test").Parse("{{/* comment */}}")
	node := convert(tpl.Root).(*pbstructs.ListNode)
	assert.Equal(t, 0, len(node.Nodes))
}

// This test shows how to enable comments in the AST.
func Test_convert_comments(t *testing.T) {
	tree := parse.New("test")
	tree.Mode = parse.ParseComments
	tree, _ = tree.Parse("{{/* comment */}}", "", "", make(map[string]*parse.Tree))
	node := convert(tree.Root).(*pbstructs.ListNode)
	assert.Equal(t, 1, len(node.Nodes))
	n1, _ := anypb.UnmarshalNew(node.Nodes[0], proto.UnmarshalOptions{})
	assert.True(t, strings.HasSuffix(node.Nodes[0].TypeUrl, "CommentNode"))
	commentNode := n1.(*pbstructs.CommentNode)
	assert.Equal(t, "/* comment */", *commentNode.Text)
}

func Test_TreeConvert(t *testing.T) {
	tests := []struct {
		name string
		tmpl string
	}{
		{
			name: "Test_TreeConvert_simple",
			tmpl: "{{ . }}",
		},
		{
			name: "Test_TreeConvert_define_and_template",
			tmpl: `
{{- /* returns "foo" */ -}}
{{- define "foo" }}
{{ slice 2 "00foo" }}
{{ end }}

{{ template "foo" }}
`,
		},
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
		},
		{
			name: "Test_TreeConvert_list_single_pipe",
			tmpl: `
{{- len ( print .Values.foo) }}
`,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			tpl, err := template.New(tt.name).Parse(tt.tmpl)
			assert.NoError(t, err)

			tree := ConvertTree(tpl.Tree)

			assert.NotNil(t, tree)
			assert.Equal(t, tt.name, *tree.Name)
			assert.Equal(t, pbstructs.NodeType_NodeList, tree.Root.NodeType)
			assert.NotEmpty(t, tree.Root.Nodes)
		})
	}
}

func Test_chain_node(t *testing.T) {
	node := parse.ChainNode{
		NodeType: parse.NodeChain,
		Pos:      10,
		Node:     nil,
		Field:    nil,
	}

	converted := convert(&node)

	assert.NotNil(t, converted)
	assert.IsType(t, &pbstructs.ChainNode{}, converted)
	chainNode := converted.(*pbstructs.ChainNode)
	assert.Equal(t, pbstructs.NodeType_NodeChain, chainNode.NodeType)
	assert.Equal(t, int64(10), chainNode.Pos)
	assert.Nil(t, chainNode.Field)
	assert.Nil(t, chainNode.Node)
}
