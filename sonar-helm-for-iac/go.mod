module github.com/SonarSource/sonar-iac/sonar-helm-for-iac

go 1.23.4

require (
	github.com/BurntSushi/toml v1.5.0
	github.com/Masterminds/sprig/v3 v3.3.0
	github.com/gobwas/glob v0.2.3
	github.com/samber/mo v1.14.0
	github.com/sonarsource/go/src v1.23.4-1
	github.com/stretchr/testify v1.10.0
	// When updating this dependency, update "google-protobuf-go" in "libs.versions.toml"
	// Also update the "PROTOBUF_GO_VERSION" in "make.sh"
	// Then run "go mod tidy" and "../gradlew generateProto"
	google.golang.org/protobuf v1.36.6
	sigs.k8s.io/yaml v1.5.0
)

require (
	dario.cat/mergo v1.0.2 // indirect
	github.com/Masterminds/goutils v1.1.1 // indirect
	github.com/Masterminds/semver/v3 v3.4.0 // indirect
	github.com/davecgh/go-spew v1.1.1 // indirect
	github.com/google/uuid v1.6.0 // indirect
	github.com/huandu/xstrings v1.5.0 // indirect
	github.com/mitchellh/copystructure v1.2.0 // indirect
	github.com/mitchellh/reflectwalk v1.0.2 // indirect
	github.com/pmezard/go-difflib v1.0.0 // indirect
	github.com/rogpeppe/go-internal v1.14.1 // indirect
	github.com/shopspring/decimal v1.4.0 // indirect
	github.com/spf13/cast v1.9.2 // indirect
	go.yaml.in/yaml/v2 v2.4.2 // indirect
	golang.org/x/crypto v0.40.0 // indirect
	gopkg.in/check.v1 v1.0.0-20201130134442-10cb98267c6c // indirect
	gopkg.in/yaml.v3 v3.0.1 // indirect
)
