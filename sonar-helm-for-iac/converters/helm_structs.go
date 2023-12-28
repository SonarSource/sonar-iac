package converters

import "fmt"

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
	"HelmVersion": "v3",
}

type Template = struct {
	Name     string
	BasePath string
}
