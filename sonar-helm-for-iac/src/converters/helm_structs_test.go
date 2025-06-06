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
	"github.com/stretchr/testify/assert"
	"testing"
)

var files = Files{
	"a.yaml":         []byte("foo: bar"),
	"c.yaml":         []byte("foo:\n  - bar"),
	"helpers/d.yaml": []byte(""),
	"e.yaml":         []byte("foo: bar\n"),
	"configs/.env":   []byte(""),
}

func Test_Files_Get(t *testing.T) {
	assert.Equal(t, "foo: bar", files.Get("a.yaml"))
	assert.Equal(t, "", files.Get("b.yaml"))
}

func Test_Files_GetBytes(t *testing.T) {
	assert.Equal(t, []byte("foo: bar"), files.GetBytes("a.yaml"))
	assert.Equal(t, make([]byte, 0), files.GetBytes("b.yaml"))
}

func Test_Files_Lines(t *testing.T) {
	assert.Equal(t, []string{"foo: bar"}, files.Lines("a.yaml"))
	assert.Equal(t, []string{"foo:", "  - bar"}, files.Lines("c.yaml"))
	assert.Equal(t, []string{"foo: bar"}, files.Lines("e.yaml"))
	assert.Equal(t, []string{}, files.Lines("non-existent"))
}

func Test_Files_Glob(t *testing.T) {
	assert.Len(t, files.Glob("*.yaml"), 3)
	assert.Len(t, files.Glob("**/*.yaml"), 1)
	// malformed glob defaults to **
	assert.Len(t, files.Glob("[a-"), 5)
}

func Test_Files_AsConfig(t *testing.T) {
	assert.Equal(t, "a.yaml: 'foo: bar'\nc.yaml: |-\n  foo:\n    - bar", files.Glob("[a|c].yaml").AsConfig())
	assert.Equal(t, "", files.Glob("not-existent").AsConfig())
}

func Test_Files_AsSecrets(t *testing.T) {
	assert.Equal(t, "a.yaml: Zm9vOiBiYXI=", files.Glob("a.yaml").AsSecrets())
	assert.Equal(t, "", files.Glob("not-existent").AsSecrets())
}

func Test_KubeVersion_GitVersion(t *testing.T) {
	kv := DefaultCapabilities["KubeVersion"].(KubeVersion)
	assert.Equal(t, "v1.20.0", kv.GitVersion)
}

func Test_Versions_Has(t *testing.T) {
	vs := DefaultCapabilities["APIVersions"].(VersionSet)
	assert.True(t, vs.Has("apps/v1"))
	assert.False(t, vs.Has("not/a/k8s/version"))
}
