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
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"os/exec"
	"strings"
	"testing"
)

// go.mod excludes golang.org/x/net, which no package of the binary uses. The exclude names one exact version, so a
// dependency update (for example, of golang.org/x/crypto) can bring another version back into the module graph.
func Test_go_mod_graph_does_not_list_golang_org_x_net(t *testing.T) {
	// "go test" puts the "go" command of the running toolchain first on the PATH.
	output, err := exec.Command("go", "mod", "graph").Output()
	require.NoError(t, err, "\"go mod graph\" failed")
	require.NotEmpty(t, output, "\"go mod graph\" printed nothing")

	edges := modGraphEdgesWithModule(string(output), "golang.org/x/net")

	assert.Empty(t, edges, "golang.org/x/net is back in the module graph: "+
		"put the listed golang.org/x/net version in the \"exclude\" directive of go.mod, then run \"go mod tidy\"")
}

func Test_modGraphEdgesWithModule(t *testing.T) {
	graph := "example.com/app example.com/lib@v1.0.0\n" +
		"example.com/app golang.org/x/net@v0.1.0\n" +
		"golang.org/x/net@v0.2.0 golang.org/x/text@v0.3.0\n" +
		"example.com/lib@v1.0.0 golang.org/x/network@v1.0.0\n" +
		"example.com/lib@v1.0.0 golang.org/x/net/sub@v1.0.0\n" +
		"example.com/lib@v1.0.0 go@1.21\n"

	edges := modGraphEdgesWithModule(graph, "golang.org/x/net")

	assert.Equal(t, []string{
		"example.com/app golang.org/x/net@v0.1.0",
		"golang.org/x/net@v0.2.0 golang.org/x/text@v0.3.0",
	}, edges)
}

func Test_modGraphEdgesWithModule_returns_nothing_for_an_empty_graph(t *testing.T) {
	assert.Empty(t, modGraphEdgesWithModule("", "golang.org/x/net"))
}

// modGraphEdgesWithModule returns the lines of "go mod graph" output in which either side is modulePath, in any version.
func modGraphEdgesWithModule(graph, modulePath string) []string {
	var edges []string
	for line := range strings.Lines(graph) {
		line = strings.TrimSpace(line)
		for _, module := range strings.Fields(line) {
			path, _, _ := strings.Cut(module, "@")
			if path == modulePath {
				edges = append(edges, line)
				break
			}
		}
	}
	return edges
}
