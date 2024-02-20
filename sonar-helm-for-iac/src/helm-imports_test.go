// SonarQube IaC Plugin
// Copyright (C) 2021-2024 SonarSource SA
// mailto:info AT sonarsource DOT com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

package main

import (
	"fmt"
	template "github.com/sonarsource/template"
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
