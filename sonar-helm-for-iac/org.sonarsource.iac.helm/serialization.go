package org_sonarsource_iac_helm

import "google.golang.org/protobuf/proto"

type Serializer interface {
	Serialize(string, error) ([]byte, error)
}

type ProtobufSerializer struct {
}

func (s ProtobufSerializer) Serialize(content string, err error) ([]byte, error) {
	errorText := ""
	if err != nil {
		errorText = err.Error()
	}
	message := TemplateEvaluationResult{
		Template: content,
		Error:    errorText,
	}
	return proto.Marshal(&message)
}
