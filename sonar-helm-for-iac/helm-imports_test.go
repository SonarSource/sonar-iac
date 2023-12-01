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
