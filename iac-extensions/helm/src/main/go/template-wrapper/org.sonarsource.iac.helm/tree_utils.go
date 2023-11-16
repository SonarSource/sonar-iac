package org_sonarsource_iac_helm

import (
	"text/template/parse"

	"google.golang.org/protobuf/proto"
	"google.golang.org/protobuf/types/known/anypb"
)

func Marshal(node parse.Node) []byte {
  node_pb := convert(node)

  out, _ := proto.Marshal(node_pb)

  return out
}

func convert(node parse.Node) proto.Message {
	var node_pb proto.Message
	switch node.(type) {
	case *parse.ActionNode:
	  node_pb = convertActionNode(*node.(*parse.ActionNode))
	case *parse.BoolNode:
	  node_pb = convertBoolNode(*node.(*parse.BoolNode))
	case *parse.BranchNode:
	  node_pb = convertBranchNode(*node.(*parse.BranchNode))
	case *parse.BreakNode:
	  node_pb = convertBreakNode(*node.(*parse.BreakNode))
	case *parse.ChainNode:
	  node_pb = convertChainNode(*node.(*parse.ChainNode))
	case *parse.CommandNode:
	  node_pb = convertCommandNode(*node.(*parse.CommandNode))
	case *parse.CommentNode:
	  node_pb = convertCommentNode(*node.(*parse.CommentNode))
	case *parse.ContinueNode:
    node_pb = convertContinueNode(*node.(*parse.ContinueNode))
  case *parse.DotNode:
    node_pb = convertDotNode(*node.(*parse.DotNode))
  case *parse.FieldNode:
    node_pb = convertFieldNode(*node.(*parse.FieldNode))
  case *parse.IdentifierNode:
    node_pb = convertIdentifierNode(*node.(*parse.IdentifierNode))
  case *parse.IfNode:
    node_pb = convertIfNode(*node.(*parse.IfNode))
  case *parse.ListNode:
    node_pb = convertListNode(*node.(*parse.ListNode))
  case *parse.NilNode:
    node_pb = convertNilNode(*node.(*parse.NilNode))
  case *parse.NumberNode:
    node_pb = convertNumberNode(*node.(*parse.NumberNode))
  case *parse.PipeNode:
    node_pb = convertPipeNode(*node.(*parse.PipeNode))
  case *parse.RangeNode:
    node_pb = convertRangeNode(*node.(*parse.RangeNode))
  case *parse.StringNode:
    node_pb = convertStringNode(*node.(*parse.StringNode))
  case *parse.TemplateNode:
    node_pb = convertTemplateNode(*node.(*parse.TemplateNode))
  case *parse.TextNode:
    node_pb = convertTextNode(*node.(*parse.TextNode))
	case *parse.VariableNode:
    node_pb = convertVariableNode(*node.(*parse.VariableNode))
  case *parse.WithNode:
    node_pb = convertWithNode(*node.(*parse.WithNode))
  default:
    node_pb = &Node{
      // NodeType duplicates declarations in parse.node, but with Unknown as 0
      NodeType: (NodeType) (int(node.Type()) + 1),
      Pos: int64(node.Position()),
    }
  }
  return node_pb
}

func convertActionNode(node parse.ActionNode) proto.Message {
  return &ActionNode{
    NodeType: NodeType_NodeAction,
    Pos: int64(node.Pos),
    Pipe: convertPipeNode(*node.Pipe),
  }
}

func convertBoolNode(node parse.BoolNode) proto.Message {
  return &BoolNode{
    NodeType: NodeType_NodeBool,
    Pos: int64(node.Pos),
    True: node.True,
  }
}

func convertBranchNode(node parse.BranchNode) proto.Message {
  var elseList *ListNode
  if node.ElseList == nil {
    elseList = nil
  } else {
    elseList = convertListNode(*node.ElseList)
  }
  return &BranchNode{
    Pos: int64(node.Pos),
    Pipe: convertPipeNode(*node.Pipe),
    List: convertListNode(*node.List),
    ElseList: elseList,
  }
}

func convertBreakNode(node parse.BreakNode) proto.Message {
  return &BreakNode{
    NodeType: NodeType_NodeBreak,
    Pos: int64(node.Pos),
  }
}

func convertChainNode(node parse.ChainNode) proto.Message {
  return &ChainNode{
    NodeType: NodeType_NodeChain,
    Pos: int64(node.Pos),
    Field: node.Field,
  }
}

func convertCommandNode(node parse.CommandNode) *CommandNode {
  args := make([]*anypb.Any, len(node.Args))
  for i, arg := range node.Args {
    any, _ := anypb.New(convert(arg))
    args[i] = any
  }
  return &CommandNode{
    NodeType: NodeType_NodeCommand,
    Pos: int64(node.Pos),
    Args: args,
  }
}

func convertCommentNode(node parse.CommentNode) proto.Message {
  return &CommentNode{
    NodeType: NodeType_NodeComment,
    Pos: int64(node.Pos),
    Text: &node.Text,
  }
}

func convertContinueNode(node parse.ContinueNode) proto.Message {
  return &ContinueNode{
    NodeType: NodeType_NodeContinue,
    Pos: int64(node.Pos),
  }
}

func convertDotNode(node parse.DotNode) proto.Message {
  return &DotNode{
    NodeType: NodeType_NodeDot,
    Pos: int64(node.Pos),
  }
}

func convertFieldNode(node parse.FieldNode) proto.Message {
  return &FieldNode{
    NodeType: NodeType_NodeField,
    Pos: int64(node.Pos),
    Ident: node.Ident,
  }
}

func convertIdentifierNode(node parse.IdentifierNode) proto.Message {
  return &IdentifierNode{
    NodeType: NodeType_NodeIdentifier,
    Pos: int64(node.Pos),
    Ident: &node.Ident,
  }
}

func convertIfNode(node parse.IfNode) proto.Message {
  return &IfNode{
    NodeType: NodeType_NodeIf,
    Pos: int64(node.Pos),
    BranchNode: convertBranchNode(node.BranchNode).(*BranchNode),
  }
}

func convertListNode(node parse.ListNode) *ListNode {
  return &ListNode{
    NodeType: NodeType_NodeList,
    Pos: int64(node.Pos),
    Nodes: convertAnyNodeList[parse.Node](node.Nodes),
  }
}

func convertNilNode(node parse.NilNode) proto.Message {
  return &NilNode{
    NodeType: NodeType_NodeNil,
    Pos: int64(node.Pos),
  }
}

func convertNumberNode(node parse.NumberNode) proto.Message {
  return &NumberNode{
    NodeType: NodeType_NodeNumber,
    Pos: int64(node.Pos),
    Text: &node.Text,
  }
}

func convertPipeNode(node parse.PipeNode) *PipeNode {
  var decls []*VariableNode = make([]*VariableNode, len(node.Decl))
  for i, decl := range node.Decl {
    decls[i] = convertVariableNode(*decl)
  }
  var cmds []*CommandNode = make([]*CommandNode, len(node.Cmds))
  for i, cmd := range node.Cmds {
    cmds[i] = convertCommandNode(*cmd)
  }
  return &PipeNode{
    NodeType: NodeType_NodePipe,
    Pos: int64(node.Pos),
    Decl: decls,
    Cmds: cmds,
  }
}

func convertRangeNode(node parse.RangeNode) proto.Message {
  return &RangeNode{
    NodeType: NodeType_NodeRange,
    Pos: int64(node.Pos),
    BranchNode: convertBranchNode(node.BranchNode).(*BranchNode),
  }
}

func convertStringNode(node parse.StringNode) proto.Message {
  return &StringNode{
    NodeType: NodeType_NodeString,
    Pos: int64(node.Pos),
    Quoted: &node.Quoted,
    Text: &node.Text,
  }
}

func convertTemplateNode(node parse.TemplateNode) proto.Message {
 return &TemplateNode{
    NodeType: NodeType_NodeTemplate,
    Pos: int64(node.Pos),
    Name: &node.Name,
  }
}

func convertTextNode(node parse.TextNode) proto.Message {
  return &TextNode{
    NodeType: NodeType_NodeText,
    Pos: int64(node.Pos),
    Text: node.Text,
  }
}

func convertVariableNode(node parse.VariableNode) *VariableNode {
  return &VariableNode{
    NodeType: NodeType_NodeVariable,
    Pos: int64(node.Pos),
    Ident: node.Ident,
  }
}

func convertWithNode(node parse.WithNode) proto.Message {
  return &WithNode{
    NodeType: NodeType_NodeWith,
    Pos: int64(node.Pos),
    BranchNode: convertBranchNode(node.BranchNode).(*BranchNode),
  }
}

func convertAnyNodeList[P parse.Node](nodes []P) []*anypb.Any {
  var result []*anypb.Any
  for _, node := range nodes {
    any, _ := anypb.New(convert(node))
    result = append(result, any)
  }
  return result
}
