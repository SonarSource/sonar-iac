// SonarQube IaC Plugin
// Copyright (C) SonarSource Sàrl
// mailto:info AT sonarsource DOT com
//
// You can redistribute and/or modify this program under the terms of
// the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the Sonar Source-Available License for more details.
//
// You should have received a copy of the Sonar Source-Available License
// along with this program; if not, see https://sonarsource.com/license/ssal/

package main

import (
	"fmt"
	"github.com/sonarsource/go/src/text/template"
	"github.com/stretchr/testify/assert"
	"math"
	"testing"
	"unsafe"
)

func Test_required(t *testing.T) {
	type args struct {
		warningMessage string
		value          interface{}
	}
	tests := []struct {
		name    string
		args    args
		want    interface{}
		wantErr assert.ErrorAssertionFunc
	}{
		{"value is nil", args{"msg", nil}, nil, assert.Error},
		{"value is not string", args{"msg", 42}, 42, assert.NoError},
		{"value is empty", args{"msg", ""}, "", assert.Error},
		{"value is aaa", args{"msg", "aaa"}, "aaa", assert.NoError},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got, err := required(tt.args.warningMessage, tt.args.value)
			if !tt.wantErr(t, err, fmt.Sprintf("required(%v, %v)", tt.args.warningMessage, tt.args.value)) {
				return
			}
			assert.Equalf(t, tt.want, got, "required(%v, %v)", tt.args.warningMessage, tt.args.value)
		})
	}
}

type ExampleStructure struct {
	A float64
}

func Test_toYamlError(t *testing.T) {

	// NaN cause UnsupportedValueError so actual should be empty string
	input := &ExampleStructure{A: math.NaN()}

	actual := toYaml(input)

	assert.Equal(t, "", actual)
}

func Test_toJsonError(t *testing.T) {
	input := &ExampleStructure{A: math.NaN()}

	actual := toJson(input)

	assert.Equal(t, "", actual)
}

func Test_toTomlError(t *testing.T) {
	type Type struct {
		A unsafe.Pointer
	}
	actual := toToml(&Type{})

	assert.Equal(t, "unsupported type: unsafe.Pointer", actual)
}

func Test_boundedBuilder_rejects_output_over_limit(t *testing.T) {
	b := boundedBuilder{limit: 5}
	_, err := b.Write([]byte("abcde"))
	assert.NoError(t, err)
	_, err = b.Write([]byte("f"))
	assert.Error(t, err)
	assert.Equal(t, "abcde", b.String())
}

func Test_repeat_with_huge_count_fails(t *testing.T) {
	repeatFn := sprigFunctions["repeat"].(func(int, string) (string, error))

	actual, err := repeatFn(2000000000, "a")

	assert.ErrorContains(t, err, "rendered template output exceeds the maximum allowed size")
	assert.Equal(t, "", actual)
}

func Test_repeat_with_negative_count_fails(t *testing.T) {
	repeatFn := sprigFunctions["repeat"].(func(int, string) (string, error))

	actual, err := repeatFn(-1, "a")

	assert.ErrorContains(t, err, "repeat count must not be negative")
	assert.Equal(t, "", actual)
}

func Test_repeat_within_limit(t *testing.T) {
	repeatFn := sprigFunctions["repeat"].(func(int, string) (string, error))

	actual, err := repeatFn(3, "ab")

	assert.NoError(t, err)
	assert.Equal(t, "ababab", actual)
}

func Test_doubling_via_print_fails(t *testing.T) {
	tmpl := template.New("test")
	tmpl.Funcs(*addCustomFunctions(tmpl))
	_, err := tmpl.New("doubling").Parse(`{{ $x := "a" }}{{ range until 40 }}{{ $x = print $x $x }}{{ end }}{{ $x }}`)
	assert.NoError(t, err)

	buf := boundedBuilder{limit: maxRenderedOutputBytes}
	err = tmpl.ExecuteTemplate(&buf, "doubling", nil)

	assert.ErrorContains(t, err, "rendered template output exceeds the maximum allowed size")
}

func Test_indent_with_huge_spaces_fails(t *testing.T) {
	tmpl := template.New("test")
	tmpl.Funcs(*addCustomFunctions(tmpl))
	_, err := tmpl.New("indenting").Parse(`{{ indent 2000000000 "a\nb" }}`)
	assert.NoError(t, err)

	buf := boundedBuilder{limit: maxRenderedOutputBytes}
	err = tmpl.ExecuteTemplate(&buf, "indenting", nil)

	assert.ErrorContains(t, err, "rendered template output exceeds the maximum allowed size")
}

func Test_indent_and_nindent_within_limit(t *testing.T) {
	indentFn := sprigFunctions["indent"].(func(int, string) (string, error))
	nindentFn := sprigFunctions["nindent"].(func(int, string) (string, error))

	indented, err := indentFn(2, "a\nb")
	assert.NoError(t, err)
	assert.Equal(t, "  a\n  b", indented)

	nindented, err := nindentFn(2, "a\nb")
	assert.NoError(t, err)
	assert.Equal(t, "\n  a\n  b", nindented)
}

func Test_include_deep_recursion(t *testing.T) {
	tmpl := template.New("test")
	funcMap := *addCustomFunctions(tmpl)
	tmpl.Funcs(funcMap)
	_, err := tmpl.New("self-referencing").Parse(`{{ include "self-referencing" . }}`)
	assert.NoError(t, err)
	includeFn := funcMap["include"].(func(string, interface{}) (string, error))

	_, err = includeFn("self-referencing", nil)

	assert.ErrorContains(t, err, "rendering t has too many recursions. Nested reference name: self-referencing")
}
