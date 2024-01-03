// SonarQube IaC Plugin
// Copyright (C) 2021-2024 SonarSource SA
// mailto:info AT sonarsource DOT com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

package converters

import (
	"bufio"
	"errors"
	"github.com/stretchr/testify/assert"
	"io"
	"strings"
	"testing"
)

func Test_read_with_empty_input(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader(""))
	stdinReader := StdinReader{}
	contents, _ := stdinReader.ReadInput(scanner)

	assert.Equal(t, 0, len(contents))
}

func Test_read_with_end_marker(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("END"))
	stdinReader := StdinReader{}
	contents, _ := stdinReader.ReadInput(scanner)

	assert.Equal(t, 0, len(contents))
}

func Test_read_n_lines_from_input(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("line1\nline2\nline3"))
	stdinReader := StdinReader{}
	lines, _ := stdinReader.readInput(scanner, 2)

	assert.Equal(t, "line1\nline2", lines)
}

func Test_read_all_lines_from_input(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("line1\nline2\nline3"))
	stdinReader := StdinReader{}
	lines, _ := stdinReader.readInput(scanner, -1)

	assert.Equal(t, "line1\nline2\nline3", lines)
}

func Test_read_one_file(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("file1\n2\nline1\nline2\nEND"))
	stdinReader := StdinReader{}
	contents, _ := stdinReader.ReadInput(scanner)

	assert.Equal(t, 1, len(contents))
	assert.Equal(t, "file1", contents[0].Name)
	assert.Equal(t, "line1\nline2", contents[0].Content)
}

func Test_read_one_file_with_trailing_newline(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("file1\n2\nline1\n \nEND"))
	stdinReader := StdinReader{}
	contents, _ := stdinReader.ReadInput(scanner)

	assert.Equal(t, 1, len(contents))
	assert.Equal(t, "file1", contents[0].Name)
	assert.Equal(t, "line1\n ", contents[0].Content)
}

func Test_read_two_files(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("file1\n2\nline1\nline2\nfile2\n1\nline3\nEND"))
	stdinReader := StdinReader{}
	contents, _ := stdinReader.ReadInput(scanner)

	assert.Equal(t, 2, len(contents))
	assert.Equal(t, "file1", contents[0].Name)
	assert.Equal(t, "line1\nline2", contents[0].Content)
	assert.Equal(t, "file2", contents[1].Name)
	assert.Equal(t, "line3", contents[1].Content)
}

func Test_read_three_files(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("file1\n2\nline1\nline2\nfile2\n1\nline3\nfile3\n1\nline4\nEND"))
	stdinReader := StdinReader{}
	contents, _ := stdinReader.ReadInput(scanner)

	assert.Equal(t, 3, len(contents))
	assert.Equal(t, "file1", contents[0].Name)
	assert.Equal(t, "line1\nline2", contents[0].Content)
	assert.Equal(t, "file2", contents[1].Name)
	assert.Equal(t, "line3", contents[1].Content)
	assert.Equal(t, "file3", contents[2].Name)
	assert.Equal(t, "line4", contents[2].Content)
}

func Test_should_stop_if_zero_length(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("file1\n0\n"))
	stdinReader := StdinReader{}
	contents, err := stdinReader.ReadInput(scanner)

	assert.Nil(t, contents)
	assert.EqualError(t, err, "request to read 0 lines aborted")
}

type ReaderWithError struct {
	numCalled int
	input     []any
}

func NewReaderWithError(input []any) *ReaderWithError {
	return &ReaderWithError{
		numCalled: 0,
		input:     input,
	}
}

func (r *ReaderWithError) Read(p []byte) (int, error) {
	defer func() {
		r.numCalled = r.numCalled + 1
	}()
	if str, ok := r.input[r.numCalled].(string); ok {
		return strings.NewReader(str).Read(p)
	} else {
		return 0, r.input[r.numCalled].(error)
	}
}

func Test_read_error_handling(t *testing.T) {
	scanner := bufio.NewScanner(NewReaderWithError([]any{io.ErrUnexpectedEOF}))
	stdinReader := StdinReader{}
	_, err := stdinReader.ReadInput(scanner)

	assert.EqualError(t, err, "unexpected EOF")
}

func Test_read_error_handling_2(t *testing.T) {
	scanner := bufio.NewScanner(NewReaderWithError([]any{"file1\n", errors.New("test read error")}))
	stdinReader := StdinReader{}
	_, err := stdinReader.ReadInput(scanner)

	assert.EqualError(t, err, "test read error")
}
