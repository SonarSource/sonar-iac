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
