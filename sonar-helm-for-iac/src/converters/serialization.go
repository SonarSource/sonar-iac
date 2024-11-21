// SonarQube IaC Plugin
// Copyright (C) 2021-2024 SonarSource SA
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
	pbstructs "github.com/SonarSource/sonar-iac/sonar-helm-for-iac/src/org.sonar.iac.helm"
	"google.golang.org/protobuf/proto"
)

type Serializer interface {
	Serialize(string, *pbstructs.Tree, error) ([]byte, error)
}

type ProtobufSerializer struct{}

func (s ProtobufSerializer) Serialize(content string, ast *pbstructs.Tree, err error) ([]byte, error) {
	errorText := ""
	if err != nil {
		errorText = err.Error()
	}
	message := pbstructs.TemplateEvaluationResult{
		Template: content,
		Error:    errorText,
		Ast:      ast,
	}
	return proto.Marshal(&message)
}
