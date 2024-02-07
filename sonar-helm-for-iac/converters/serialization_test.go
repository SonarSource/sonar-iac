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

package converters

import (
	"fmt"
	pbstructs "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/org.sonarsource.iac.helm"
	"github.com/stretchr/testify/assert"
	"google.golang.org/protobuf/proto"
	"testing"
	"text/template"
)

func Test_ProtobufSerializer_Serialize(t *testing.T) {
	serializer := ProtobufSerializer{}
	content := "content"
	bytes, err := serializer.Serialize(content, nil, nil)

	result := pbstructs.TemplateEvaluationResult{}
	err = proto.Unmarshal(bytes, &result)
	assert.Nil(t, err)
	assert.Equal(t, content, result.Template)
	assert.Equal(t, "", result.Error)
}

func Test_ProtobufSerializer_Serialize_With_Error_Text(t *testing.T) {
	serializer := ProtobufSerializer{}
	content := "content"
	err := fmt.Errorf("error text")
	bytes, err := serializer.Serialize(content, nil, err)

	result := pbstructs.TemplateEvaluationResult{}
	err = proto.Unmarshal(bytes, &result)
	assert.Nil(t, err)
	assert.Equal(t, content, result.Template)
	assert.Equal(t, "error text", result.Error)
}

func Test_ProtobufSerializer_Serialize_With_Ast(t *testing.T) {
	serializer := ProtobufSerializer{}
	content := "content"
	tpl, _ := template.New("test").Parse("{{ . }}")
	ast := tpl.Tree
	bytes, err := serializer.Serialize(content, ast, nil)

	result := pbstructs.TemplateEvaluationResult{}
	err = proto.Unmarshal(bytes, &result)
	assert.NoError(t, err)
	assert.Equal(t, content, result.Template)
	assert.NotNil(t, result.Ast)
}
