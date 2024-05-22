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

type InputReaderMock struct {
	Name     string
	Contents Files
}

func (i *InputReaderMock) ReadInput(*bufio.Scanner) (string, Files, error) {
	return i.Name, i.Contents, nil
}

func Test_read_with_empty_input(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader(""))
	stdinReader := StdinReader{}
	_, contents, _ := stdinReader.ReadInput(scanner)

	assert.Equal(t, 0, len(contents))
}

func Test_read_with_end_marker(t *testing.T) {
	scanner := bufio.NewScanner(strings.NewReader("END"))
	stdinReader := StdinReader{}
	_, contents, _ := stdinReader.ReadInput(scanner)

	assert.Equal(t, 0, len(contents))
}

func Test_read_n_lines_from_input(t *testing.T) {
	text := "line1\nline2\nline3"
	expected := "line1\nline2\n"
	scanner := CreateScanner(strings.NewReader(text))
	stdinReader := StdinReader{}
	lines, _ := stdinReader.readInput(scanner, 2)

	assert.Equal(t, []byte(expected), lines)
}

func Test_read_all_lines_from_input(t *testing.T) {
	scanner := CreateScanner(strings.NewReader("line1\nline2\nline3"))
	stdinReader := StdinReader{}
	lines, _ := stdinReader.readInput(scanner, -1)

	assert.Equal(t, []byte("line1\nline2\nline3"), lines)
}

func Test_read_all_lines_from_input_different_new_lines(t *testing.T) {
	text := "line1\r\nline2\nline3\rline4\u2028line5\u2029line6"
	scanner := CreateScanner(strings.NewReader(text))
	stdinReader := StdinReader{}
	lines, _ := stdinReader.readInput(scanner, len(text))

	assert.Equal(t, []byte(text), lines)
}

func Test_read_all_lines_from_input_different_new_lines2(t *testing.T) {
	text := "line1\u2028line2"
	scanner := CreateScanner(strings.NewReader(text))
	stdinReader := StdinReader{}
	lines, _ := stdinReader.readInput(scanner, len(text))

	assert.Equal(t, []byte(text), lines)
}

func Test_read_one_file(t *testing.T) {
	scanner := CreateScanner(strings.NewReader("file1\n2\nline1\nline2\nEND\n"))
	stdinReader := StdinReader{}
	_, contents, _ := stdinReader.ReadInput(scanner)

	assert.Equal(t, 1, len(contents))
	assert.Contains(t, contents, "file1")
	assert.Equal(t, []byte("line1\nline2\n"), contents["file1"])
}

func Test_read_one_file_with_trailing_newline(t *testing.T) {
	scanner := CreateScanner(strings.NewReader("file1\n2\nline1\n \nEND\n"))
	stdinReader := StdinReader{}
	_, contents, _ := stdinReader.ReadInput(scanner)

	assert.Equal(t, 1, len(contents))
	assert.Contains(t, contents, "file1")
	assert.Equal(t, []byte("line1\n \n"), contents["file1"])
}

func Test_read_two_files(t *testing.T) {
	scanner := CreateScanner(strings.NewReader("file1\n2\nline1\nline2\nfile2\n1\nline3\nEND\n"))
	stdinReader := StdinReader{}
	_, contents, _ := stdinReader.ReadInput(scanner)

	assert.Equal(t, 2, len(contents))
	assert.Contains(t, contents, "file1")
	assert.Equal(t, []byte("line1\nline2\n"), contents["file1"])
	assert.Contains(t, contents, "file2")
	assert.Equal(t, "line3\n", string(contents["file2"]))
}

func Test_read_three_files(t *testing.T) {
	scanner := CreateScanner(strings.NewReader("file1\n2\nline1\nline2\nfile2\n1\nline3\nfile3\n1\nline4\nEND\n"))
	stdinReader := StdinReader{}
	_, contents, _ := stdinReader.ReadInput(scanner)

	assert.Equal(t, 3, len(contents))
	assert.Contains(t, contents, "file1")
	assert.Equal(t, []byte("line1\nline2\n"), contents["file1"])
	assert.Contains(t, contents, "file2")
	assert.Equal(t, "line3\n", string(contents["file2"]))
	assert.Contains(t, contents, "file3")
	assert.Equal(t, "line4\n", string(contents["file3"]))
}

func Test_read_zero_length(t *testing.T) {
	scanner := CreateScanner(strings.NewReader("file1\n0\n\nEND\n"))
	stdinReader := StdinReader{}
	_, contents, err := stdinReader.ReadInput(scanner)

	assert.Equal(t, 1, len(contents))
	assert.Contains(t, contents, "file1")
	assert.Equal(t, []byte(""), contents["file1"])
	assert.Nil(t, err)
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
	_, _, err := stdinReader.ReadInput(scanner)

	assert.EqualError(t, err, "unexpected EOF")
}

func Test_read_error_handling_2(t *testing.T) {
	scanner := bufio.NewScanner(NewReaderWithError([]any{"file1\n", errors.New("test read error")}))
	stdinReader := StdinReader{}
	_, _, err := stdinReader.ReadInput(scanner)

	assert.EqualError(t, err, "test read error")
}

func Test_read_wrong_format(t *testing.T) {
	scanner := CreateScanner(strings.NewReader("file1\nNOTaNUMBER\n\nEND"))
	stdinReader := StdinReader{}
	_, contents, err := stdinReader.ReadInput(scanner)

	assert.Nil(t, contents)
	assert.EqualError(t, err, "strconv.Atoi: parsing \"NOTaNUMBER\": invalid syntax")
}

func Test_no_file_provided(t *testing.T) {
	err := validateInput(Files{})

	assert.NotNil(t, err)
	assert.Equal(t, "no input received", err.Error())
}

func Test_only_one_file_provided(t *testing.T) {
	err := validateInput(Files{
		"a.yaml": []byte("apiVersion: v1"),
	})

	assert.NoError(t, err)
}

// TODO fix it
//func Test_two_files_provided(t *testing.T) {
//	stdinReader = &InputReaderMock{
//		Name: "a.yaml",
//		Contents: Files{
//			"templates/a.yaml": []byte("apiVersion: v1"),
//			"values.yaml":      []byte("foo: bar"),
//		},
//	}
//
//	_, err := ReadAndValidateSources()
//
//	// verify that method does not crash and this code is reached
//	assert.Nil(t, err)
//}

func Test_template_struct_from_2_sources(t *testing.T) {
	sources := Files{
		"templates/a.yaml": []byte("apiVersion: v1"),
		"values.yaml":      []byte("foo: bar"),
	}

	templateSources := NewTemplateSourcesFromRawSources("a.yaml", sources)

	assert.Equal(t, 2, templateSources.NumSources())
}

func Test_template_struct_from_3_sources(t *testing.T) {
	sources := Files{
		"templates/a.yaml": []byte("apiVersion: v1"),
		"_helpers.tpl":     []byte("{{/* comment */}}"),
		"values.yaml":      []byte("foo: bar"),
	}

	templateSources := NewTemplateSourcesFromRawSources("a.yaml", sources)

	assert.Equal(t, 3, templateSources.NumSources())
	assert.Equal(t, "foo: bar", templateSources.Values())
}
