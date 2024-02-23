// Copyright 2011 The Go Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

// Parse nodes.

package parse

import (
	"fmt"
	"strconv"
	"strings"
)

var textFormat = "%s" // Changed to "%q" in tests for better error messages.

// A Node is an element in the parse tree. The interface is trivial.
// The interface contains an unexported method so that only
// types local to this package can satisfy it.
type Node interface {
	Type() NodeType
	String() string
	// Copy does a deep copy of the Node and all its components.
	// To avoid type assertions, some XxxNodes also have specialized
	// CopyXxx methods that return *XxxNode.
	Copy() Node
	StartOffset() Pos // byte position of start of node in full original input string
	Length() int      // length of node in bytes in full original input string
	// tree returns the containing *Tree.
	// It is unexported so all implementations of Node are in this package.
	tree() *Tree
	// writeTo writes the String output to the builder.
	writeTo(*strings.Builder)
}

// NodeType identifies the type of a parse tree node.
type NodeType int

// Pos represents a byte position in the original input text from which
// this template was parsed.
type Pos int

func (p Pos) Position() Pos {
	return p
}

type Location struct {
	startOffset Pos
	length      int
}

func (l Location) StartOffset() Pos {
	return l.startOffset
}

func (l Location) Length() int {
	return l.length
}

// Type returns itself and provides an easy default implementation
// for embedding in a Node. Embedded in all non-trivial Nodes.
func (t NodeType) Type() NodeType {
	return t
}

const (
	NodeText       NodeType = iota // Plain text.
	NodeAction                     // A non-control action such as a field evaluation.
	NodeBool                       // A boolean constant.
	NodeChain                      // A sequence of field accesses.
	NodeCommand                    // An element of a pipeline.
	NodeDot                        // The cursor, dot.
	nodeElse                       // An else action. Not added to tree.
	nodeEnd                        // An end action. Not added to tree.
	NodeField                      // A field or method name.
	NodeIdentifier                 // An identifier; always a function name.
	NodeIf                         // An if action.
	NodeList                       // A list of Nodes.
	NodeNil                        // An untyped nil constant.
	NodeNumber                     // A numerical constant.
	NodePipe                       // A pipeline of commands.
	NodeRange                      // A range action.
	NodeString                     // A string constant.
	NodeTemplate                   // A template invocation action.
	NodeVariable                   // A $ variable.
	NodeWith                       // A with action.
	NodeComment                    // A comment.
	NodeBreak                      // A break action.
	NodeContinue                   // A continue action.
)

// Nodes.

// ListNode holds a sequence of nodes.
type ListNode struct {
	NodeType
	Location
	tr    *Tree
	Nodes []Node // The element nodes in lexical order.
}

func (t *Tree) newList(start Pos) *ListNode {
	return &ListNode{tr: t, NodeType: NodeList, Location: Location{start, -1}}
}

func (l *ListNode) append(n Node) {
	l.Nodes = append(l.Nodes, n)
}

func (l *ListNode) tree() *Tree {
	return l.tr
}

func (l *ListNode) String() string {
	var sb strings.Builder
	l.writeTo(&sb)
	return sb.String()
}

func (l *ListNode) writeTo(sb *strings.Builder) {
	for _, n := range l.Nodes {
		n.writeTo(sb)
	}
}

func (l *ListNode) CopyList() *ListNode {
	if l == nil {
		return l
	}
	n := l.tr.newList(l.startOffset)
	n.length = l.length
	for _, elem := range l.Nodes {
		n.append(elem.Copy())
	}
	return n
}

func (l *ListNode) Copy() Node {
	return l.CopyList()
}

// TextNode holds plain text.
type TextNode struct {
	NodeType
	Location
	tr   *Tree
	Text []byte // The text; may span newlines.
}

func (t *Tree) newText(loc Location, text string) *TextNode {
	return &TextNode{tr: t, NodeType: NodeText, Location: loc, Text: []byte(text)}
}

func (t *TextNode) String() string {
	return fmt.Sprintf(textFormat, t.Text)
}

func (t *TextNode) writeTo(sb *strings.Builder) {
	sb.WriteString(t.String())
}

func (t *TextNode) tree() *Tree {
	return t.tr
}

func (t *TextNode) Copy() Node {
	return &TextNode{tr: t.tr, NodeType: NodeText, Location: t.Location, Text: append([]byte{}, t.Text...)}
}

// CommentNode holds a comment.
type CommentNode struct {
	NodeType
	Location
	tr   *Tree
	Text string // Comment text.
}

func (t *Tree) newComment(loc Location, text string) *CommentNode {
	return &CommentNode{tr: t, NodeType: NodeComment, Location: loc, Text: text}
}

func (c *CommentNode) String() string {
	var sb strings.Builder
	c.writeTo(&sb)
	return sb.String()
}

func (c *CommentNode) writeTo(sb *strings.Builder) {
	sb.WriteString("{{")
	sb.WriteString(c.Text)
	sb.WriteString("}}")
}

func (c *CommentNode) tree() *Tree {
	return c.tr
}

func (c *CommentNode) Copy() Node {
	return &CommentNode{tr: c.tr, NodeType: NodeComment, Location: c.Location, Text: c.Text}
}

// PipeNode holds a pipeline with optional declaration
type PipeNode struct {
	NodeType
	Location
	tr       *Tree
	Line     int             // The line number in the input. Deprecated: Kept for compatibility.
	IsAssign bool            // The variables are being assigned, not declared.
	Decl     []*VariableNode // Variables in lexical order.
	Cmds     []*CommandNode  // The commands in lexical order.
}

func (t *Tree) newPipeline(startOffset Pos, line int, vars []*VariableNode) *PipeNode {
	return &PipeNode{tr: t, NodeType: NodePipe, Location: Location{startOffset, -1}, Line: line, Decl: vars}
}

func (p *PipeNode) append(command *CommandNode) {
	p.Cmds = append(p.Cmds, command)
}

func (p *PipeNode) String() string {
	var sb strings.Builder
	p.writeTo(&sb)
	return sb.String()
}

func (p *PipeNode) writeTo(sb *strings.Builder) {
	if len(p.Decl) > 0 {
		for i, v := range p.Decl {
			if i > 0 {
				sb.WriteString(", ")
			}
			v.writeTo(sb)
		}
		sb.WriteString(" := ")
	}
	for i, c := range p.Cmds {
		if i > 0 {
			sb.WriteString(" | ")
		}
		c.writeTo(sb)
	}
}

func (p *PipeNode) tree() *Tree {
	return p.tr
}

func (p *PipeNode) CopyPipe() *PipeNode {
	if p == nil {
		return p
	}
	vars := make([]*VariableNode, len(p.Decl))
	for i, d := range p.Decl {
		vars[i] = d.Copy().(*VariableNode)
	}
	n := p.tr.newPipeline(p.StartOffset(), p.Line, vars)
	n.length = p.length
	n.IsAssign = p.IsAssign
	for _, c := range p.Cmds {
		n.append(c.Copy().(*CommandNode))
	}
	return n
}

func (p *PipeNode) Copy() Node {
	return p.CopyPipe()
}

// ActionNode holds an action (something bounded by delimiters).
// Control actions have their own nodes; ActionNode represents simple
// ones such as field evaluations and parenthesized pipelines.
type ActionNode struct {
	NodeType
	Location
	tr   *Tree
	Line int       // The line number in the input. Deprecated: Kept for compatibility.
	Pipe *PipeNode // The pipeline in the action.
}

func (t *Tree) newAction(loc Location, line int, pipe *PipeNode) *ActionNode {
	return &ActionNode{tr: t, NodeType: NodeAction, Location: loc, Line: line, Pipe: pipe}
}

func (a *ActionNode) String() string {
	var sb strings.Builder
	a.writeTo(&sb)
	return sb.String()
}

func (a *ActionNode) writeTo(sb *strings.Builder) {
	sb.WriteString("{{")
	a.Pipe.writeTo(sb)
	sb.WriteString("}}")
}

func (a *ActionNode) tree() *Tree {
	return a.tr
}

func (a *ActionNode) Copy() Node {
	return a.tr.newAction(a.Location, a.Line, a.Pipe.CopyPipe())

}

// CommandNode holds a command (a pipeline inside an evaluating action).
type CommandNode struct {
	NodeType
	Location
	Value string
	tr    *Tree
	Args  []Node // Arguments in lexical order: Identifier, field, or constant.
}

func (t *Tree) newCommand(loc Location, value string) *CommandNode {
	return &CommandNode{tr: t, NodeType: NodeCommand, Location: loc, Value: value}
}

func (c *CommandNode) append(arg Node) {
	c.Args = append(c.Args, arg)
}

func (c *CommandNode) String() string {
	var sb strings.Builder
	c.writeTo(&sb)
	return sb.String()
}

func (c *CommandNode) writeTo(sb *strings.Builder) {
	for i, arg := range c.Args {
		if i > 0 {
			sb.WriteByte(' ')
		}
		if arg, ok := arg.(*PipeNode); ok {
			sb.WriteByte('(')
			arg.writeTo(sb)
			sb.WriteByte(')')
			continue
		}
		arg.writeTo(sb)
	}
}

func (c *CommandNode) tree() *Tree {
	return c.tr
}

func (c *CommandNode) Copy() Node {
	if c == nil {
		return c
	}
	n := c.tr.newCommand(c.Location, c.Value)
	for _, c := range c.Args {
		n.append(c.Copy())
	}
	return n
}

// IdentifierNode holds an identifier.
type IdentifierNode struct {
	NodeType
	Location
	tr    *Tree
	Ident string // The identifier's name.
}

// NewIdentifier returns a new IdentifierNode with the given identifier name.
func (t *Tree) newIdentifier(ident string, loc Location) *IdentifierNode {
	return &IdentifierNode{NodeType: NodeIdentifier, Ident: ident, tr: t, Location: loc}
}

func (i *IdentifierNode) String() string {
	return i.Ident
}

func (i *IdentifierNode) writeTo(sb *strings.Builder) {
	sb.WriteString(i.String())
}

func (i *IdentifierNode) tree() *Tree {
	return i.tr
}

func (i *IdentifierNode) Copy() Node {
	return i.tr.newIdentifier(i.Ident, i.Location)
}

// VariableNode holds a list of variable names, possibly with chained field
// accesses. The dollar sign is part of the (first) name.
type VariableNode struct {
	NodeType
	Location
	tr    *Tree
	Ident []string // Variable name and fields in the order they appear.
}

func (t *Tree) newVariable(loc Location, ident string) *VariableNode {
	return &VariableNode{tr: t, NodeType: NodeVariable, Location: loc, Ident: strings.Split(ident, ".")}
}

func (v *VariableNode) String() string {
	var sb strings.Builder
	v.writeTo(&sb)
	return sb.String()
}

func (v *VariableNode) writeTo(sb *strings.Builder) {
	for i, id := range v.Ident {
		if i > 0 {
			sb.WriteByte('.')
		}
		sb.WriteString(id)
	}
}

func (v *VariableNode) tree() *Tree {
	return v.tr
}

func (v *VariableNode) Copy() Node {
	return &VariableNode{tr: v.tr, NodeType: NodeVariable, Location: v.Location, Ident: append([]string{}, v.Ident...)}
}

// DotNode holds the special identifier '.'.
type DotNode struct {
	NodeType
	Location
	tr *Tree
}

func (t *Tree) newDot(loc Location) *DotNode {
	return &DotNode{tr: t, NodeType: NodeDot, Location: loc}
}

func (d *DotNode) Type() NodeType {
	// Override method on embedded NodeType for API compatibility.
	// TODO: Not really a problem; could change API without effect but
	// api tool complains.
	return NodeDot
}

func (d *DotNode) String() string {
	return "."
}

func (d *DotNode) writeTo(sb *strings.Builder) {
	sb.WriteString(d.String())
}

func (d *DotNode) tree() *Tree {
	return d.tr
}

func (d *DotNode) Copy() Node {
	return d.tr.newDot(d.Location)
}

// NilNode holds the special identifier 'nil' representing an untyped nil constant.
type NilNode struct {
	NodeType
	Location
	tr *Tree
}

func (t *Tree) newNil(loc Location) *NilNode {
	return &NilNode{tr: t, NodeType: NodeNil, Location: loc}
}

func (n *NilNode) Type() NodeType {
	// Override method on embedded NodeType for API compatibility.
	// TODO: Not really a problem; could change API without effect but
	// api tool complains.
	return NodeNil
}

func (n *NilNode) String() string {
	return "nil"
}

func (n *NilNode) writeTo(sb *strings.Builder) {
	sb.WriteString(n.String())
}

func (n *NilNode) tree() *Tree {
	return n.tr
}

func (n *NilNode) Copy() Node {
	return n.tr.newNil(n.Location)
}

// FieldNode holds a field (identifier starting with '.').
// The names may be chained ('.x.y').
// The period is dropped from each ident.
type FieldNode struct {
	NodeType
	Location
	tr    *Tree
	Ident []string // The identifiers in the order they appear, without dots.
}

func (t *Tree) newField(loc Location, ident string) *FieldNode {
	return &FieldNode{tr: t, NodeType: NodeField, Location: loc, Ident: strings.Split(ident[1:], ".")} // [1:] to drop leading period
}

func (f *FieldNode) String() string {
	var sb strings.Builder
	f.writeTo(&sb)
	return sb.String()
}

func (f *FieldNode) writeTo(sb *strings.Builder) {
	for _, id := range f.Ident {
		sb.WriteByte('.')
		sb.WriteString(id)
	}
}

func (f *FieldNode) tree() *Tree {
	return f.tr
}

func (f *FieldNode) Copy() Node {
	return &FieldNode{tr: f.tr, NodeType: NodeField, Location: f.Location, Ident: append([]string{}, f.Ident...)}
}

// ChainNode holds a term followed by a chain of field accesses (identifier starting with '.').
// The names may be chained ('.x.y').
// The periods are dropped from each ident.
type ChainNode struct {
	NodeType
	Location
	tr    *Tree
	Node  Node
	Field []string // The identifiers in lexical order.
}

func (t *Tree) newChain(start Pos, node Node) *ChainNode {
	// Initialize length with 0, it will be updated when fields are added.
	return &ChainNode{tr: t, NodeType: NodeChain, Location: Location{start, 0}, Node: node}
}

// Add adds the named field (which should start with a period) to the end of the chain.
func (c *ChainNode) Add(itemField item) {
	field := itemField.val
	if len(field) == 0 || field[0] != '.' {
		panic("no dot in field")
	}
	field = field[1:] // Remove leading dot.
	if field == "" {
		panic("empty field")
	}
	c.Field = append(c.Field, field)
	c.length += itemField.length()
}

func (c *ChainNode) String() string {
	var sb strings.Builder
	c.writeTo(&sb)
	return sb.String()
}

func (c *ChainNode) writeTo(sb *strings.Builder) {
	if _, ok := c.Node.(*PipeNode); ok {
		sb.WriteByte('(')
		c.Node.writeTo(sb)
		sb.WriteByte(')')
	} else {
		c.Node.writeTo(sb)
	}
	for _, field := range c.Field {
		sb.WriteByte('.')
		sb.WriteString(field)
	}
}

func (c *ChainNode) tree() *Tree {
	return c.tr
}

func (c *ChainNode) Copy() Node {
	return &ChainNode{tr: c.tr, NodeType: NodeChain, Location: c.Location, Node: c.Node, Field: append([]string{}, c.Field...)}
}

// BoolNode holds a boolean constant.
type BoolNode struct {
	NodeType
	Location
	tr   *Tree
	True bool // The value of the boolean constant.
}

func (t *Tree) newBool(loc Location, true bool) *BoolNode {
	return &BoolNode{tr: t, NodeType: NodeBool, Location: loc, True: true}
}

func (b *BoolNode) String() string {
	if b.True {
		return "true"
	}
	return "false"
}

func (b *BoolNode) writeTo(sb *strings.Builder) {
	sb.WriteString(b.String())
}

func (b *BoolNode) tree() *Tree {
	return b.tr
}

func (b *BoolNode) Copy() Node {
	return b.tr.newBool(b.Location, b.True)
}

// NumberNode holds a number: signed or unsigned integer, float, or complex.
// The value is parsed and stored under all the types that can represent the value.
// This simulates in a small amount of code the behavior of Go's ideal constants.
type NumberNode struct {
	NodeType
	Location
	tr         *Tree
	IsInt      bool       // Number has an integral value.
	IsUint     bool       // Number has an unsigned integral value.
	IsFloat    bool       // Number has a floating-point value.
	IsComplex  bool       // Number is complex.
	Int64      int64      // The signed integer value.
	Uint64     uint64     // The unsigned integer value.
	Float64    float64    // The floating-point value.
	Complex128 complex128 // The complex value.
	Text       string     // The original textual representation from the input.
}

func (t *Tree) newNumber(loc Location, text string, typ itemType) (*NumberNode, error) {
	n := &NumberNode{tr: t, NodeType: NodeNumber, Location: loc, Text: text}
	switch typ {
	case itemCharConstant:
		rune, _, tail, err := strconv.UnquoteChar(text[1:], text[0])
		if err != nil {
			return nil, err
		}
		if tail != "'" {
			return nil, fmt.Errorf("malformed character constant: %s", text)
		}
		n.Int64 = int64(rune)
		n.IsInt = true
		n.Uint64 = uint64(rune)
		n.IsUint = true
		n.Float64 = float64(rune) // odd but those are the rules.
		n.IsFloat = true
		return n, nil
	case itemComplex:
		// fmt.Sscan can parse the pair, so let it do the work.
		if _, err := fmt.Sscan(text, &n.Complex128); err != nil {
			return nil, err
		}
		n.IsComplex = true
		n.simplifyComplex()
		return n, nil
	}
	// Imaginary constants can only be complex unless they are zero.
	if len(text) > 0 && text[len(text)-1] == 'i' {
		f, err := strconv.ParseFloat(text[:len(text)-1], 64)
		if err == nil {
			n.IsComplex = true
			n.Complex128 = complex(0, f)
			n.simplifyComplex()
			return n, nil
		}
	}
	// Do integer test first so we get 0x123 etc.
	u, err := strconv.ParseUint(text, 0, 64) // will fail for -0; fixed below.
	if err == nil {
		n.IsUint = true
		n.Uint64 = u
	}
	i, err := strconv.ParseInt(text, 0, 64)
	if err == nil {
		n.IsInt = true
		n.Int64 = i
		if i == 0 {
			n.IsUint = true // in case of -0.
			n.Uint64 = u
		}
	}
	// If an integer extraction succeeded, promote the float.
	if n.IsInt {
		n.IsFloat = true
		n.Float64 = float64(n.Int64)
	} else if n.IsUint {
		n.IsFloat = true
		n.Float64 = float64(n.Uint64)
	} else {
		f, err := strconv.ParseFloat(text, 64)
		if err == nil {
			// If we parsed it as a float but it looks like an integer,
			// it's a huge number too large to fit in an int. Reject it.
			if !strings.ContainsAny(text, ".eEpP") {
				return nil, fmt.Errorf("integer overflow: %q", text)
			}
			n.IsFloat = true
			n.Float64 = f
			// If a floating-point extraction succeeded, extract the int if needed.
			if !n.IsInt && float64(int64(f)) == f {
				n.IsInt = true
				n.Int64 = int64(f)
			}
			if !n.IsUint && float64(uint64(f)) == f {
				n.IsUint = true
				n.Uint64 = uint64(f)
			}
		}
	}
	if !n.IsInt && !n.IsUint && !n.IsFloat {
		return nil, fmt.Errorf("illegal number syntax: %q", text)
	}
	return n, nil
}

// simplifyComplex pulls out any other types that are represented by the complex number.
// These all require that the imaginary part be zero.
func (n *NumberNode) simplifyComplex() {
	n.IsFloat = imag(n.Complex128) == 0
	if n.IsFloat {
		n.Float64 = real(n.Complex128)
		n.IsInt = float64(int64(n.Float64)) == n.Float64
		if n.IsInt {
			n.Int64 = int64(n.Float64)
		}
		n.IsUint = float64(uint64(n.Float64)) == n.Float64
		if n.IsUint {
			n.Uint64 = uint64(n.Float64)
		}
	}
}

func (n *NumberNode) String() string {
	return n.Text
}

func (n *NumberNode) writeTo(sb *strings.Builder) {
	sb.WriteString(n.String())
}

func (n *NumberNode) tree() *Tree {
	return n.tr
}

func (n *NumberNode) Copy() Node {
	nn := new(NumberNode)
	*nn = *n // Easy, fast, correct.
	return nn
}

// StringNode holds a string constant. The value has been "unquoted".
type StringNode struct {
	NodeType
	Location
	tr     *Tree
	Quoted string // The original text of the string, with quotes.
	Text   string // The string, after quote processing.
}

func (t *Tree) newString(loc Location, orig, unQuoted string) *StringNode {
	return &StringNode{tr: t, NodeType: NodeString, Location: loc, Quoted: orig, Text: unQuoted}
}

func (s *StringNode) String() string {
	return s.Quoted
}

func (s *StringNode) writeTo(sb *strings.Builder) {
	sb.WriteString(s.String())
}

func (s *StringNode) tree() *Tree {
	return s.tr
}

func (s *StringNode) Copy() Node {
	return s.tr.newString(s.Location, s.Quoted, s.Text)
}

// endNode represents an {{end}} action.
// It does not appear in the final parse tree.
type endNode struct {
	NodeType
	Location
	tr *Tree
}

func (t *Tree) newEnd(loc Location) *endNode {
	return &endNode{tr: t, NodeType: nodeEnd, Location: loc}
}

func (e *endNode) String() string {
	return "{{end}}"
}

func (e *endNode) writeTo(sb *strings.Builder) {
	sb.WriteString(e.String())
}

func (e *endNode) tree() *Tree {
	return e.tr
}

func (e *endNode) Copy() Node {
	return e.tr.newEnd(e.Location)
}

// elseNode represents an {{else}} action. Does not appear in the final tree.
type elseNode struct {
	NodeType
	Location
	tr   *Tree
	Line int // The line number in the input. Deprecated: Kept for compatibility.
}

func (t *Tree) newElse(loc Location, line int) *elseNode {
	return &elseNode{tr: t, NodeType: nodeElse, Location: loc, Line: line}
}

func (e *elseNode) Type() NodeType {
	return nodeElse
}

func (e *elseNode) String() string {
	return "{{else}}"
}

func (e *elseNode) writeTo(sb *strings.Builder) {
	sb.WriteString(e.String())
}

func (e *elseNode) tree() *Tree {
	return e.tr
}

func (e *elseNode) Copy() Node {
	return e.tr.newElse(e.Location, e.Line)
}

// BranchNode is the common representation of if, range, and with.
type BranchNode struct {
	NodeType
	Location
	tr       *Tree
	Line     int       // The line number in the input. Deprecated: Kept for compatibility.
	Pipe     *PipeNode // The pipeline to be evaluated.
	List     *ListNode // What to execute if the value is non-empty.
	ElseList *ListNode // What to execute if the value is empty (nil if absent).
}

func (b *BranchNode) String() string {
	var sb strings.Builder
	b.writeTo(&sb)
	return sb.String()
}

func (b *BranchNode) writeTo(sb *strings.Builder) {
	name := ""
	switch b.NodeType {
	case NodeIf:
		name = "if"
	case NodeRange:
		name = "range"
	case NodeWith:
		name = "with"
	default:
		panic("unknown branch type")
	}
	sb.WriteString("{{")
	sb.WriteString(name)
	sb.WriteByte(' ')
	b.Pipe.writeTo(sb)
	sb.WriteString("}}")
	b.List.writeTo(sb)
	if b.ElseList != nil {
		sb.WriteString("{{else}}")
		b.ElseList.writeTo(sb)
	}
	sb.WriteString("{{end}}")
}

func (b *BranchNode) tree() *Tree {
	return b.tr
}

func (b *BranchNode) Copy() Node {
	switch b.NodeType {
	case NodeIf:
		return b.tr.newIf(b.Location, b.Line, b.Pipe, b.List, b.ElseList)
	case NodeRange:
		return b.tr.newRange(b.Location, b.Line, b.Pipe, b.List, b.ElseList)
	case NodeWith:
		return b.tr.newWith(b.Location, b.Line, b.Pipe, b.List, b.ElseList)
	default:
		panic("unknown branch type")
	}
}

// IfNode represents an {{if}} action and its commands.
type IfNode struct {
	BranchNode
}

func (t *Tree) newIf(loc Location, line int, pipe *PipeNode, list, elseList *ListNode) *IfNode {
	return &IfNode{BranchNode{tr: t, NodeType: NodeIf, Location: loc, Line: line, Pipe: pipe, List: list, ElseList: elseList}}
}

func (i *IfNode) Copy() Node {
	return i.tr.newIf(i.Location, i.Line, i.Pipe.CopyPipe(), i.List.CopyList(), i.ElseList.CopyList())
}

// BreakNode represents a {{break}} action.
type BreakNode struct {
	tr *Tree
	NodeType
	Location
	Line int
}

func (t *Tree) newBreak(loc Location, line int) *BreakNode {
	return &BreakNode{tr: t, NodeType: NodeBreak, Location: loc, Line: line}
}

func (b *BreakNode) Copy() Node                  { return b.tr.newBreak(b.Location, b.Line) }
func (b *BreakNode) String() string              { return "{{break}}" }
func (b *BreakNode) tree() *Tree                 { return b.tr }
func (b *BreakNode) writeTo(sb *strings.Builder) { sb.WriteString("{{break}}") }

// ContinueNode represents a {{continue}} action.
type ContinueNode struct {
	tr *Tree
	NodeType
	Location
	Line int
}

func (t *Tree) newContinue(loc Location, line int) *ContinueNode {
	return &ContinueNode{tr: t, NodeType: NodeContinue, Location: loc, Line: line}
}

func (c *ContinueNode) Copy() Node                  { return c.tr.newContinue(c.Location, c.Line) }
func (c *ContinueNode) String() string              { return "{{continue}}" }
func (c *ContinueNode) tree() *Tree                 { return c.tr }
func (c *ContinueNode) writeTo(sb *strings.Builder) { sb.WriteString("{{continue}}") }

// RangeNode represents a {{range}} action and its commands.
type RangeNode struct {
	BranchNode
}

func (t *Tree) newRange(loc Location, line int, pipe *PipeNode, list, elseList *ListNode) *RangeNode {
	return &RangeNode{BranchNode{tr: t, NodeType: NodeRange, Location: loc, Line: line, Pipe: pipe, List: list, ElseList: elseList}}
}

func (r *RangeNode) Copy() Node {
	return r.tr.newRange(r.Location, r.Line, r.Pipe.CopyPipe(), r.List.CopyList(), r.ElseList.CopyList())
}

// WithNode represents a {{with}} action and its commands.
type WithNode struct {
	BranchNode
}

func (t *Tree) newWith(loc Location, line int, pipe *PipeNode, list, elseList *ListNode) *WithNode {
	return &WithNode{BranchNode{tr: t, NodeType: NodeWith, Location: loc, Line: line, Pipe: pipe, List: list, ElseList: elseList}}
}

func (w *WithNode) Copy() Node {
	return w.tr.newWith(w.Location, w.Line, w.Pipe.CopyPipe(), w.List.CopyList(), w.ElseList.CopyList())
}

// TemplateNode represents a {{template}} action.
type TemplateNode struct {
	NodeType
	Location
	tr   *Tree
	Line int       // The line number in the input. Deprecated: Kept for compatibility.
	Name string    // The name of the template (unquoted).
	Pipe *PipeNode // The command to evaluate as dot for the template.
}

func (t *Tree) newTemplate(loc Location, line int, name string, pipe *PipeNode) *TemplateNode {
	return &TemplateNode{tr: t, NodeType: NodeTemplate, Location: loc, Line: line, Name: name, Pipe: pipe}
}

func (t *TemplateNode) String() string {
	var sb strings.Builder
	t.writeTo(&sb)
	return sb.String()
}

func (t *TemplateNode) writeTo(sb *strings.Builder) {
	sb.WriteString("{{template ")
	sb.WriteString(strconv.Quote(t.Name))
	if t.Pipe != nil {
		sb.WriteByte(' ')
		t.Pipe.writeTo(sb)
	}
	sb.WriteString("}}")
}

func (t *TemplateNode) tree() *Tree {
	return t.tr
}

func (t *TemplateNode) Copy() Node {
	return t.tr.newTemplate(t.Location, t.Line, t.Name, t.Pipe.CopyPipe())
}
