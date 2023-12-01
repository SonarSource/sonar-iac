// SonarQube IaC Plugin
// Copyright (C) 2021-2023 SonarSource SA
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
	"github.com/stretchr/testify/assert"
	"testing"
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
