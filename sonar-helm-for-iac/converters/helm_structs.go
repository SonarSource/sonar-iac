package converters

import (
	"encoding/base64"
	"fmt"
	"github.com/gobwas/glob"
	"path"
	"sigs.k8s.io/yaml"
	"strings"
)

// https://helm.sh/docs/chart_template_guide/builtin_objects/

type Values = map[string]interface{}

// Chart represents content of Chart.yaml. Since we don't check validity, we can load it as a map of any.
// Helm checks validity and forces presence of required fields, so we can assume that they are present, and presumably
// template won't reference any fields that are not present.
type Chart = map[string]interface{}

var DefaultReleaseMetadata = map[string]interface{}{
	"Name":      "helm-chart",
	"Namespace": "default",
	"IsUpgrade": false,
	"IsInstall": true,
	"Revision":  1,
	"Service":   "Helm",
}

// We don't have a K8S connection, so this is filled with some reasonable values.
const k8sVersionMajor = "1"
const k8sVersionMinor = "20"

var DefaultCapabilities = map[string]interface{}{
	"KubeVersion": struct {
		Version string
		Major   string
		Minor   string
	}{
		fmt.Sprintf("v%s.%s.0", k8sVersionMajor, k8sVersionMinor),
		k8sVersionMajor,
		k8sVersionMinor,
	},
	// It is possible to directly use `k8s.io/apimachinery/pkg/runtime/scheme.go`, as does Helm. Not sure if we need it.
	"APIVersions": []string{"v1"},
	"HelmVersion": struct {
		Version      string
		GitCommit    string
		GitTreeState string
		GoVersion    string
	}{
		"v3.5",
		"276121c8693b48978eae5c09602b1e74d9a2a7e6",
		"clean",
		"go1.21",
	},
}

type Template = struct {
	Name     string
	BasePath string
}

// Files stores all the files that are needed to evaluate a template with their contents as bytes.
// Methods of this struct are the same as in Helm; and as in Helm they fall back to empty values if something is not valid.
type Files map[string][]byte

func (files Files) GetBytes(name string) []byte {
	if content, ok := files[name]; ok {
		return content
	}
	return []byte{}
}

func (files Files) Get(name string) string {
	return string(files.GetBytes(name))
}

// Glob matches files by provided pattern. Helm uses third-party glob library, because Go's glob from stdlib doesn't support e.g. `**`.
func (files Files) Glob(pattern string) Files {
	globMatcher, err := glob.Compile(pattern, '/')
	if err != nil {
		globMatcher, _ = glob.Compile("**")
	}

	matched := Files{}
	for name, contents := range files {
		if globMatcher.Match(name) {
			matched[name] = contents
		}
	}

	return matched
}

func (files Files) AsConfig() string {
	if files == nil || len(files) == 0 {
		return ""
	}

	m := make(map[string]string)

	for filename, content := range files {
		m[path.Base(filename)] = string(content)
	}

	return toYAML(m)
}

func (files Files) AsSecrets() string {
	if files == nil || len(files) == 0 {
		return ""
	}

	m := make(map[string]string)

	for filename, content := range files {
		m[path.Base(filename)] = base64.StdEncoding.EncodeToString(content)
	}

	return toYAML(m)
}

func (files Files) Lines(path string) []string {
	if files == nil || files[path] == nil {
		return []string{}
	}
	s := string(files[path])
	if s[len(s)-1] == '\n' {
		s = s[:len(s)-1]
	}
	return strings.Split(s, "\n")
}

func toYAML(o any) string {
	data, err := yaml.Marshal(o)
	if err != nil {
		return ""
	}
	return strings.TrimSuffix(string(data), "\n")
}
