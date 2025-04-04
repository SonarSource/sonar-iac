// SonarQube IaC Plugin
// Copyright (C) 2021-2025 SonarSource SA
// mailto:info AT sonarsource DOT com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
