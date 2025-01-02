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

package converters

import (
	"fmt"
	pbstructs "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/src/org.sonar.iac.helm"
	"github.com/sonarsource/go/src/text/template"
	"github.com/stretchr/testify/assert"
	"google.golang.org/protobuf/proto"
	"testing"
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
	content := "{{ . }}"
	converter := TestConverter{}
	tpl, _ := template.New("test").Parse(content)
	ast := converter.ConvertTree(content, tpl.Tree)
	bytes, err := serializer.Serialize(content, ast, nil)

	result := pbstructs.TemplateEvaluationResult{}
	err = proto.Unmarshal(bytes, &result)
	assert.NoError(t, err)
	assert.Equal(t, content, result.Template)
	assert.NotNil(t, result.Ast)
}
