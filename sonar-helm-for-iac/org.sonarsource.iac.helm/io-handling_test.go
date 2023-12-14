package org_sonarsource_iac_helm

import (
	"bufio"
	"github.com/stretchr/testify/assert"
	"strings"
	"testing"
)

func Test_read_with_empty_input(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader(""))
	stdinReader := StdinReader{}
	contents := stdinReader.ReadInput(scanner)

	assert.Equal(t, 0, len(contents))
}

func Test_read_with_end_marker(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("END"))
	stdinReader := StdinReader{}
	contents := stdinReader.ReadInput(scanner)

	assert.Equal(t, 0, len(contents))
}

func Test_read_n_lines_from_input(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("line1\nline2\nline3"))
	stdinReader := StdinReader{}
	lines := stdinReader.readInput(scanner, 2)

	assert.Equal(t, "line1\nline2", lines)
}

func Test_read_all_lines_from_input(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("line1\nline2\nline3"))
	stdinReader := StdinReader{}
	lines := stdinReader.readInput(scanner, -1)

	assert.Equal(t, "line1\nline2\nline3", lines)
}

func Test_read_one_file(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("file1\n2\nline1\nline2\nEND"))
	stdinReader := StdinReader{}
	contents := stdinReader.ReadInput(scanner)

	assert.Equal(t, 1, len(contents))
	assert.Equal(t, "file1", contents[0].Name)
	assert.Equal(t, "line1\nline2", contents[0].Content)
}

func Test_read_one_file_with_trailing_newline(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("file1\n2\nline1\n \nEND"))
	stdinReader := StdinReader{}
	contents := stdinReader.ReadInput(scanner)

	assert.Equal(t, 1, len(contents))
	assert.Equal(t, "file1", contents[0].Name)
	assert.Equal(t, "line1\n ", contents[0].Content)
}

func Test_read_two_files(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("file1\n2\nline1\nline2\nfile2\n1\nline3\nEND"))
	stdinReader := StdinReader{}
	contents := stdinReader.ReadInput(scanner)

	assert.Equal(t, 2, len(contents))
	assert.Equal(t, "file1", contents[0].Name)
	assert.Equal(t, "line1\nline2", contents[0].Content)
	assert.Equal(t, "file2", contents[1].Name)
	assert.Equal(t, "line3", contents[1].Content)
}
