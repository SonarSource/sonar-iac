package converters

import (
	"fmt"
	pbstructs "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/src/org.sonar.iac.helm"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/reflect/protoreflect"
	"google.golang.org/protobuf/types/known/anypb"
	"os"
	"text/template/parse"
)

var KwBreak = "break"
var KwContinue = "continue"
var KwNil = "nil"
var KwTrue = "true"
var KwFalse = "false"

type Converter interface {
	ConvertTree(string, *parse.Tree) *pbstructs.Tree
}

type DefaultConverter struct{}

type ConversionContext struct {
	Content   string
	Converter Converter
}

func (c *DefaultConverter) ConvertTree(source string, ast *parse.Tree) *pbstructs.Tree {
	if ast == nil {
		return nil
	}

	context := ConversionContext{
		Content:   source,
		Converter: c,
	}

	mode := uint64(ast.Mode)
	tree := &pbstructs.Tree{
		Name:      &ast.Name,
		ParseName: &ast.ParseName,
		Mode:      &mode,
	}
	// To support conversion of arbitrary Node types, `Convert` returns a `proto.Message`.
	// But for the tree root, we expect a specific type - `ListNode`. Setting it with protobuf reflection seems to be sufficient.
	tree.ProtoReflect().Set(
		tree.ProtoReflect().Descriptor().Fields().ByName("root"),
		protoreflect.ValueOf(context.Convert(&*ast.Root).ProtoReflect()))
	return tree
}

func (c ConversionContext) Convert(node parse.Node) proto.Message {
	var nodeAsMessage proto.Message
	switch node.(type) {
	case *parse.ActionNode:
		nodeAsMessage = c.convertActionNode(*node.(*parse.ActionNode))
	case *parse.BoolNode:
		nodeAsMessage = c.convertBoolNode(*node.(*parse.BoolNode))
	// There is no `case *parse.BranchNode` as it is a base type which wouldn't be encountered directly.
	case *parse.BreakNode:
		nodeAsMessage = c.convertBreakNode(*node.(*parse.BreakNode))
	case *parse.ChainNode:
		nodeAsMessage = c.convertChainNode(*node.(*parse.ChainNode))
	case *parse.CommandNode:
		nodeAsMessage = c.convertCommandNode(*node.(*parse.CommandNode))
	case *parse.CommentNode:
		nodeAsMessage = c.convertCommentNode(*node.(*parse.CommentNode))
	case *parse.ContinueNode:
		nodeAsMessage = c.convertContinueNode(*node.(*parse.ContinueNode))
	case *parse.DotNode:
		nodeAsMessage = c.convertDotNode(*node.(*parse.DotNode))
	case *parse.FieldNode:
		nodeAsMessage = c.convertFieldNode(*node.(*parse.FieldNode))
	case *parse.IdentifierNode:
		nodeAsMessage = c.convertIdentifierNode(*node.(*parse.IdentifierNode))
	case *parse.IfNode:
		nodeAsMessage = c.convertIfNode(*node.(*parse.IfNode))
	case *parse.ListNode:
		nodeAsMessage = c.convertListNode(*node.(*parse.ListNode))
	case *parse.NilNode:
		nodeAsMessage = c.convertNilNode(*node.(*parse.NilNode))
	case *parse.NumberNode:
		nodeAsMessage = c.convertNumberNode(*node.(*parse.NumberNode))
	case *parse.PipeNode:
		nodeAsMessage = c.convertPipeNode(*node.(*parse.PipeNode))
	case *parse.RangeNode:
		nodeAsMessage = c.convertRangeNode(*node.(*parse.RangeNode))
	case *parse.StringNode:
		nodeAsMessage = c.convertStringNode(*node.(*parse.StringNode))
	case *parse.TemplateNode:
		nodeAsMessage = c.convertTemplateNode(*node.(*parse.TemplateNode))
	case *parse.TextNode:
		nodeAsMessage = c.convertTextNode(*node.(*parse.TextNode))
	case *parse.VariableNode:
		nodeAsMessage = c.convertVariableNode(*node.(*parse.VariableNode))
	case *parse.WithNode:
		nodeAsMessage = c.convertWithNode(*node.(*parse.WithNode))
	default:
		fmt.Fprintf(os.Stderr, "Unknown node type: %T\n", node)
		nodeAsMessage = &pbstructs.Node{
			// NodeType duplicates declarations in parse.node, but with Unknown as 0
			NodeType: (pbstructs.NodeType)(int(node.Type()) + 1),
			Pos:      int64(node.Position()),
		}
	}

	return nodeAsMessage
}

func (c ConversionContext) convertActionNode(node parse.ActionNode) proto.Message {
	return &pbstructs.ActionNode{
		NodeType: pbstructs.NodeType_NodeAction,
		Pos:      int64(node.Position()),
		Length:   nodeSourceLength(&node),
		Pipe:     c.convertPipeNode(*node.Pipe),
	}
}

func (c ConversionContext) convertBoolNode(node parse.BoolNode) proto.Message {
	var length int
	if node.True {
		length = len(KwTrue)
	} else {
		length = len(KwFalse)
	}
	return &pbstructs.BoolNode{
		NodeType: pbstructs.NodeType_NodeBool,
		Pos:      int64(node.Position()),
		Length:   int64(length),
		True:     node.True,
	}
}

func (c ConversionContext) convertBranchNode(node parse.BranchNode) proto.Message {
	var elseList *pbstructs.ListNode
	if node.ElseList == nil {
		elseList = nil
	} else {
		elseList = c.convertListNode(*node.ElseList)
	}
	return &pbstructs.BranchNode{
		Pos:      int64(node.Position()),
		Length:   nodeSourceLength(&node),
		Pipe:     c.convertPipeNode(*node.Pipe),
		List:     c.convertListNode(*node.List),
		ElseList: elseList,
	}
}

func (c ConversionContext) convertBreakNode(node parse.BreakNode) proto.Message {
	return &pbstructs.BreakNode{
		NodeType: pbstructs.NodeType_NodeBreak,
		Pos:      int64(node.Position()),
		Length:   int64(len(KwBreak)),
	}
}

func (c ConversionContext) convertChainNode(node parse.ChainNode) proto.Message {
	return &pbstructs.ChainNode{
		NodeType: pbstructs.NodeType_NodeChain,
		Pos:      int64(node.Pos),
		Length:   nodeSourceLength(&node),
		Field:    node.Field,
	}
}

func (c ConversionContext) convertCommandNode(node parse.CommandNode) *pbstructs.CommandNode {
	args := make([]*anypb.Any, len(node.Args))
	for i, arg := range node.Args {
		argAsAny, _ := anypb.New(c.Convert(arg))
		args[i] = argAsAny
	}
	return &pbstructs.CommandNode{
		NodeType: pbstructs.NodeType_NodeCommand,
		Pos:      int64(node.Position()),
		Length:   nodeSourceLength(&node),
		Args:     args,
	}
}

func (c ConversionContext) convertCommentNode(node parse.CommentNode) proto.Message {
	return &pbstructs.CommentNode{
		NodeType: pbstructs.NodeType_NodeComment,
		Pos:      int64(node.Position()),
		Length:   int64(len(node.Text)),
		Text:     &node.Text,
	}
}

func (c ConversionContext) convertContinueNode(node parse.ContinueNode) proto.Message {
	return &pbstructs.ContinueNode{
		NodeType: pbstructs.NodeType_NodeContinue,
		Pos:      int64(node.Position()),
		Length:   int64(len(KwContinue)),
	}
}

func (c ConversionContext) convertDotNode(node parse.DotNode) proto.Message {
	return &pbstructs.DotNode{
		NodeType: pbstructs.NodeType_NodeDot,
		Pos:      int64(node.Position()),
		Length:   1,
	}
}

func (c ConversionContext) convertFieldNode(node parse.FieldNode) proto.Message {
	return &pbstructs.FieldNode{
		NodeType: pbstructs.NodeType_NodeField,
		Pos:      int64(node.Position()),
		Length:   nodeSourceLength(&node),
		Ident:    node.Ident,
	}
}

func (c ConversionContext) convertIdentifierNode(node parse.IdentifierNode) proto.Message {
	return &pbstructs.IdentifierNode{
		NodeType: pbstructs.NodeType_NodeIdentifier,
		Pos:      int64(node.Position()),
		Length:   nodeSourceLength(&node),
		Ident:    &node.Ident,
	}
}

func (c ConversionContext) convertIfNode(node parse.IfNode) proto.Message {
	return &pbstructs.IfNode{
		NodeType:   pbstructs.NodeType_NodeIf,
		Pos:        int64(node.Pos),
		Length:     nodeSourceLength(&node),
		BranchNode: c.convertBranchNode(node.BranchNode).(*pbstructs.BranchNode),
	}
}

func (c ConversionContext) convertListNode(node parse.ListNode) *pbstructs.ListNode {
	return &pbstructs.ListNode{
		NodeType: pbstructs.NodeType_NodeList,
		Pos:      int64(node.Position()),
		Length:   nodeSourceLength(&node),
		Nodes:    c.convertAnyNodeList(node.Nodes),
	}
}

func (c ConversionContext) convertNilNode(node parse.NilNode) proto.Message {
	return &pbstructs.NilNode{
		NodeType: pbstructs.NodeType_NodeNil,
		Pos:      int64(node.Position()),
		Length:   int64(len(KwNil)),
	}
}

func (c ConversionContext) convertNumberNode(node parse.NumberNode) proto.Message {
	return &pbstructs.NumberNode{
		NodeType: pbstructs.NodeType_NodeNumber,
		Pos:      int64(node.Position()),
		Length:   int64(len(node.Text)),
		Text:     &node.Text,
	}
}

func (c ConversionContext) convertPipeNode(node parse.PipeNode) *pbstructs.PipeNode {
	var decls = make([]*pbstructs.VariableNode, len(node.Decl))
	for i, decl := range node.Decl {
		decls[i] = c.convertVariableNode(*decl)
	}
	var cmds = make([]*pbstructs.CommandNode, len(node.Cmds))
	for i, cmd := range node.Cmds {
		cmds[i] = c.convertCommandNode(*cmd)
	}
	return &pbstructs.PipeNode{
		NodeType: pbstructs.NodeType_NodePipe,
		Pos:      int64(node.Position()),
		Length:   nodeSourceLength(&node),
		Decl:     decls,
		Cmds:     cmds,
	}
}

func (c ConversionContext) convertRangeNode(node parse.RangeNode) proto.Message {
	return &pbstructs.RangeNode{
		NodeType:   pbstructs.NodeType_NodeRange,
		Pos:        int64(node.Pos),
		Length:     nodeSourceLength(&node),
		BranchNode: c.convertBranchNode(node.BranchNode).(*pbstructs.BranchNode),
	}
}

func (c ConversionContext) convertStringNode(node parse.StringNode) proto.Message {
	return &pbstructs.StringNode{
		NodeType: pbstructs.NodeType_NodeString,
		Pos:      int64(node.Position()),
		Length:   int64(len(node.Quoted)),
		Quoted:   &node.Quoted,
		Text:     &node.Text,
	}
}

func (c ConversionContext) convertTemplateNode(node parse.TemplateNode) proto.Message {
	return &pbstructs.TemplateNode{
		NodeType: pbstructs.NodeType_NodeTemplate,
		Pos:      int64(node.Position()),
		Length:   nodeSourceLength(&node),
		Name:     &node.Name,
	}
}

func (c ConversionContext) convertTextNode(node parse.TextNode) proto.Message {
	return &pbstructs.TextNode{
		NodeType: pbstructs.NodeType_NodeText,
		Pos:      int64(node.Position()),
		Length:   int64(len(node.Text)),
		Text:     node.Text,
	}
}

func (c ConversionContext) convertVariableNode(node parse.VariableNode) *pbstructs.VariableNode {
	return &pbstructs.VariableNode{
		NodeType: pbstructs.NodeType_NodeVariable,
		Pos:      int64(node.Position()),
		Length:   nodeSourceLength(&node),
		Ident:    node.Ident,
	}
}

func (c ConversionContext) convertWithNode(node parse.WithNode) proto.Message {
	return &pbstructs.WithNode{
		NodeType:   pbstructs.NodeType_NodeWith,
		Pos:        int64(node.Pos),
		Length:     nodeSourceLength(&node),
		BranchNode: c.convertBranchNode(node.BranchNode).(*pbstructs.BranchNode),
	}
}

func (c ConversionContext) convertAnyNodeList(nodes []parse.Node) []*anypb.Any {
	var result []*anypb.Any
	for _, node := range nodes {
		nodeAsAny, _ := anypb.New(c.Convert(node))
		result = append(result, nodeAsAny)
	}
	return result
}

// nodeSourceLength returns the length of the source code represented by the node.
// `text/template/parse` doesn't store the original text, not gives it an easy way to access it.
// Node representation returned by `.String()` is sometimes inaccurate and doesn't include spacing, but it's our best bet as the first step.
// Note: sometimes `pos` points not to the start of the node, but to the start of the first token in the node.
func nodeSourceLength(node parse.Node) int64 {
	return int64(len(node.String()))
}
