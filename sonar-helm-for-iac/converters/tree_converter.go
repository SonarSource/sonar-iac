package converters

import (
	"fmt"
	pbstructs "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/org.sonar.iac.helm"
	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/reflect/protoreflect"
	"google.golang.org/protobuf/types/known/anypb"
	"os"
	"text/template/parse"
)

func ConvertTree(ast *parse.Tree) *pbstructs.Tree {
	mode := uint64(ast.Mode)
	tree := &pbstructs.Tree{
		Name:      &ast.Name,
		ParseName: &ast.ParseName,
		Mode:      &mode,
	}
	// To support conversion of arbitrary Node types, `convert` returns a `proto.Message`.
	// But for the tree root, we expect a specific type - `ListNode`. Setting it with protobuf reflection seems to be sufficient.
	tree.ProtoReflect().Set(
		tree.ProtoReflect().Descriptor().Fields().ByName("root"),
		protoreflect.ValueOf(convert(&*ast.Root).ProtoReflect()))
	return tree
}

func convert(node parse.Node) proto.Message {
	var nodeAsMessage proto.Message
	switch node.(type) {
	case *parse.ActionNode:
		nodeAsMessage = convertActionNode(*node.(*parse.ActionNode))
	case *parse.BoolNode:
		nodeAsMessage = convertBoolNode(*node.(*parse.BoolNode))
	// There is no `case *parse.BranchNode` as it is a base type which wouldn't be encountered directly.
	case *parse.BreakNode:
		nodeAsMessage = convertBreakNode(*node.(*parse.BreakNode))
	case *parse.ChainNode:
		nodeAsMessage = convertChainNode(*node.(*parse.ChainNode))
	case *parse.CommandNode:
		nodeAsMessage = convertCommandNode(*node.(*parse.CommandNode))
	case *parse.CommentNode:
		nodeAsMessage = convertCommentNode(*node.(*parse.CommentNode))
	case *parse.ContinueNode:
		nodeAsMessage = convertContinueNode(*node.(*parse.ContinueNode))
	case *parse.DotNode:
		nodeAsMessage = convertDotNode(*node.(*parse.DotNode))
	case *parse.FieldNode:
		nodeAsMessage = convertFieldNode(*node.(*parse.FieldNode))
	case *parse.IdentifierNode:
		nodeAsMessage = convertIdentifierNode(*node.(*parse.IdentifierNode))
	case *parse.IfNode:
		nodeAsMessage = convertIfNode(*node.(*parse.IfNode))
	case *parse.ListNode:
		nodeAsMessage = convertListNode(*node.(*parse.ListNode))
	case *parse.NilNode:
		nodeAsMessage = convertNilNode(*node.(*parse.NilNode))
	case *parse.NumberNode:
		nodeAsMessage = convertNumberNode(*node.(*parse.NumberNode))
	case *parse.PipeNode:
		nodeAsMessage = convertPipeNode(*node.(*parse.PipeNode))
	case *parse.RangeNode:
		nodeAsMessage = convertRangeNode(*node.(*parse.RangeNode))
	case *parse.StringNode:
		nodeAsMessage = convertStringNode(*node.(*parse.StringNode))
	case *parse.TemplateNode:
		nodeAsMessage = convertTemplateNode(*node.(*parse.TemplateNode))
	case *parse.TextNode:
		nodeAsMessage = convertTextNode(*node.(*parse.TextNode))
	case *parse.VariableNode:
		nodeAsMessage = convertVariableNode(*node.(*parse.VariableNode))
	case *parse.WithNode:
		nodeAsMessage = convertWithNode(*node.(*parse.WithNode))
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

func convertActionNode(node parse.ActionNode) proto.Message {
	return &pbstructs.ActionNode{
		NodeType: pbstructs.NodeType_NodeAction,
		Pos:      int64(node.Pos),
		Pipe:     convertPipeNode(*node.Pipe),
	}
}

func convertBoolNode(node parse.BoolNode) proto.Message {
	return &pbstructs.BoolNode{
		NodeType: pbstructs.NodeType_NodeBool,
		Pos:      int64(node.Pos),
		True:     node.True,
	}
}

func convertBranchNode(node parse.BranchNode) proto.Message {
	var elseList *pbstructs.ListNode
	if node.ElseList == nil {
		elseList = nil
	} else {
		elseList = convertListNode(*node.ElseList)
	}
	return &pbstructs.BranchNode{
		Pos:      int64(node.Pos),
		Pipe:     convertPipeNode(*node.Pipe),
		List:     convertListNode(*node.List),
		ElseList: elseList,
	}
}

func convertBreakNode(node parse.BreakNode) proto.Message {
	return &pbstructs.BreakNode{
		NodeType: pbstructs.NodeType_NodeBreak,
		Pos:      int64(node.Pos),
	}
}

func convertChainNode(node parse.ChainNode) proto.Message {
	return &pbstructs.ChainNode{
		NodeType: pbstructs.NodeType_NodeChain,
		Pos:      int64(node.Pos),
		Field:    node.Field,
	}
}

func convertCommandNode(node parse.CommandNode) *pbstructs.CommandNode {
	args := make([]*anypb.Any, len(node.Args))
	for i, arg := range node.Args {
		argAsAny, _ := anypb.New(convert(arg))
		args[i] = argAsAny
	}
	return &pbstructs.CommandNode{
		NodeType: pbstructs.NodeType_NodeCommand,
		Pos:      int64(node.Pos),
		Args:     args,
	}
}

func convertCommentNode(node parse.CommentNode) proto.Message {
	return &pbstructs.CommentNode{
		NodeType: pbstructs.NodeType_NodeComment,
		Pos:      int64(node.Pos),
		Text:     &node.Text,
	}
}

func convertContinueNode(node parse.ContinueNode) proto.Message {
	return &pbstructs.ContinueNode{
		NodeType: pbstructs.NodeType_NodeContinue,
		Pos:      int64(node.Pos),
	}
}

func convertDotNode(node parse.DotNode) proto.Message {
	return &pbstructs.DotNode{
		NodeType: pbstructs.NodeType_NodeDot,
		Pos:      int64(node.Pos),
	}
}

func convertFieldNode(node parse.FieldNode) proto.Message {
	return &pbstructs.FieldNode{
		NodeType: pbstructs.NodeType_NodeField,
		Pos:      int64(node.Pos),
		Ident:    node.Ident,
	}
}

func convertIdentifierNode(node parse.IdentifierNode) proto.Message {
	return &pbstructs.IdentifierNode{
		NodeType: pbstructs.NodeType_NodeIdentifier,
		Pos:      int64(node.Pos),
		Ident:    &node.Ident,
	}
}

func convertIfNode(node parse.IfNode) proto.Message {
	return &pbstructs.IfNode{
		NodeType:   pbstructs.NodeType_NodeIf,
		Pos:        int64(node.Pos),
		BranchNode: convertBranchNode(node.BranchNode).(*pbstructs.BranchNode),
	}
}

func convertListNode(node parse.ListNode) *pbstructs.ListNode {
	return &pbstructs.ListNode{
		NodeType: pbstructs.NodeType_NodeList,
		Pos:      int64(node.Pos),
		Nodes:    convertAnyNodeList[parse.Node](node.Nodes),
	}
}

func convertNilNode(node parse.NilNode) proto.Message {
	return &pbstructs.NilNode{
		NodeType: pbstructs.NodeType_NodeNil,
		Pos:      int64(node.Pos),
	}
}

func convertNumberNode(node parse.NumberNode) proto.Message {
	return &pbstructs.NumberNode{
		NodeType: pbstructs.NodeType_NodeNumber,
		Pos:      int64(node.Pos),
		Text:     &node.Text,
	}
}

func convertPipeNode(node parse.PipeNode) *pbstructs.PipeNode {
	var decls = make([]*pbstructs.VariableNode, len(node.Decl))
	for i, decl := range node.Decl {
		decls[i] = convertVariableNode(*decl)
	}
	var cmds = make([]*pbstructs.CommandNode, len(node.Cmds))
	for i, cmd := range node.Cmds {
		cmds[i] = convertCommandNode(*cmd)
	}
	return &pbstructs.PipeNode{
		NodeType: pbstructs.NodeType_NodePipe,
		Pos:      int64(node.Pos),
		Decl:     decls,
		Cmds:     cmds,
	}
}

func convertRangeNode(node parse.RangeNode) proto.Message {
	return &pbstructs.RangeNode{
		NodeType:   pbstructs.NodeType_NodeRange,
		Pos:        int64(node.Pos),
		BranchNode: convertBranchNode(node.BranchNode).(*pbstructs.BranchNode),
	}
}

func convertStringNode(node parse.StringNode) proto.Message {
	return &pbstructs.StringNode{
		NodeType: pbstructs.NodeType_NodeString,
		Pos:      int64(node.Pos),
		Quoted:   &node.Quoted,
		Text:     &node.Text,
	}
}

func convertTemplateNode(node parse.TemplateNode) proto.Message {
	return &pbstructs.TemplateNode{
		NodeType: pbstructs.NodeType_NodeTemplate,
		Pos:      int64(node.Pos),
		Name:     &node.Name,
	}
}

func convertTextNode(node parse.TextNode) proto.Message {
	return &pbstructs.TextNode{
		NodeType: pbstructs.NodeType_NodeText,
		Pos:      int64(node.Pos),
		Text:     node.Text,
	}
}

func convertVariableNode(node parse.VariableNode) *pbstructs.VariableNode {
	return &pbstructs.VariableNode{
		NodeType: pbstructs.NodeType_NodeVariable,
		Pos:      int64(node.Pos),
		Ident:    node.Ident,
	}
}

func convertWithNode(node parse.WithNode) proto.Message {
	return &pbstructs.WithNode{
		NodeType:   pbstructs.NodeType_NodeWith,
		Pos:        int64(node.Pos),
		BranchNode: convertBranchNode(node.BranchNode).(*pbstructs.BranchNode),
	}
}

func convertAnyNodeList[P parse.Node](nodes []P) []*anypb.Any {
	var result []*anypb.Any
	for _, node := range nodes {
		nodeAsAny, _ := anypb.New(convert(node))
		result = append(result, nodeAsAny)
	}
	return result
}
