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
	"github.com/stretchr/testify/assert"
	"os"
	"strings"
	"testing"
	"time"
)

func Test_read_single_template(t *testing.T) {
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x00\x0Dtemplate.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x00"))
	templateName, contents, err := ReadInput(input)

	assert.NoError(t, err)
	assert.Equal(t, "template.yaml", templateName)
	assert.Equal(t, 1, len(contents))
	assert.Equal(t, "apiVersion: v1", contents.Get("template.yaml"))
}

func Test_read_long_template_name(t *testing.T) {
	filename := strings.Repeat("/very/long/path", 100) + "/template.yaml"
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x05\xEA" + filename + "\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x00"))
	templateName, contents, err := ReadInput(input)

	assert.NoError(t, err)
	assert.Equal(t, filename, templateName)
	assert.Equal(t, 1, len(contents))
	assert.Equal(t, "apiVersion: v1", contents.Get(filename))
}

func Test_read_template_and_empty_values(t *testing.T) {
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x01\x00\x00\x00\x0Bvalues.yaml\x00\x00\x00\x00"))
	templateName, contents, err := ReadInput(input)

	assert.NoError(t, err)
	assert.Equal(t, "foo.yaml", templateName)
	assert.Equal(t, 2, len(contents))
	assert.Equal(t, "apiVersion: v1", contents.Get("foo.yaml"))
	assert.Equal(t, "", contents.Get("values.yaml"))
}

func Test_read_with_empty_input(t *testing.T) {
	timeout := time.After(1 * time.Second)
	done := make(chan bool)
	go func() {
		input, output, _ := os.Pipe()
		output.Write([]byte(""))
		ReadInput(input)
		done <- true
	}()

	select {
	case <-done:
		t.Fatal("It should be timeout for empty input")
	case <-timeout:
	}
}

func Test_read_n_lines_from_input(t *testing.T) {
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x01" +
		"\x00\x00\x00\x0Bvalues.yaml\x00\x00\x00\x11line1\nline2\nline3"))
	_, contents, err := ReadInput(input)

	assert.NoError(t, err)
	assert.Equal(t, 2, len(contents))
	assert.Equal(t, "line1\nline2\nline3", contents.Get("values.yaml"))
}

func Test_read_all_lines_from_input_different_new_lines(t *testing.T) {
	// the \u2028 character uses 3 bytes
	content := "line1\r\nline2\nline3\rline4\u2028line5\u2029line6"
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x01" +
		"\x00\x00\x00\x0Bvalues.yaml\x00\x00\x00\x28" + content))
	_, contents, err := ReadInput(input)

	assert.NoError(t, err)
	assert.Equal(t, 2, len(contents))
	assert.Equal(t, content, contents.Get("values.yaml"))
}

func Test_read_one_file_with_trailing_newline(t *testing.T) {
	content := "line1\n"
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x01" +
		"\x00\x00\x00\x0Bvalues.yaml\x00\x00\x00\x06" + content))
	_, contents, err := ReadInput(input)

	assert.NoError(t, err)
	assert.Equal(t, 2, len(contents))
	assert.Equal(t, content, contents.Get("values.yaml"))
}

func Test_read_three_files(t *testing.T) {
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x03" +
		"\x00\x00\x00\x0Bvalues.yaml\x00\x00\x00\x0Bline1\nline2" +
		"\x00\x00\x00\x12templates/foo.yaml\x00\x00\x00\x05line3" +
		"\x00\x00\x00\x16templates/_helpers.tpl\x00\x00\x00\x08\nline4\r\n"))
	_, contents, err := ReadInput(input)

	assert.NoError(t, err)
	assert.Equal(t, 4, len(contents))
	assert.Equal(t, "line1\nline2", contents.Get("values.yaml"))
	assert.Equal(t, "line3", contents.Get("templates/foo.yaml"))
	assert.Equal(t, "\nline4\r\n", contents.Get("templates/_helpers.tpl"))
}

func Test_read_error_handling(t *testing.T) {
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00"))
	output.Close()
	_, _, err := ReadInput(input)

	assert.Equal(t, "Error reading from stdin, expecting to read 4 bytes, but got 1", err.Error())
}

func Test_read_error_handling_2(t *testing.T) {
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x00\x11foo"))
	output.Close()
	_, _, err := ReadInput(input)

	assert.Equal(t, "Error reading from stdin, expecting to read 17 bytes, but got 3", err.Error())
}

func Test_read_error_handling_3(t *testing.T) {
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00"))
	output.Close()
	_, _, err := ReadInput(input)

	assert.Equal(t, "Error reading from stdin, expecting to read 4 bytes, but got 2", err.Error())
}

func Test_read_error_handling_4(t *testing.T) {
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersi"))
	output.Close()
	_, _, err := ReadInput(input)

	assert.Equal(t, "Error reading from stdin, expecting to read 14 bytes, but got 8", err.Error())
}

func Test_read_error_handling_5(t *testing.T) {
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00"))
	output.Close()
	_, _, err := ReadInput(input)

	assert.Equal(t, "Error reading from stdin, expecting to read 4 bytes, but got 1", err.Error())
}

func Test_read_error_handling_6(t *testing.T) {
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x03"))
	output.Close()
	_, _, err := ReadInput(input)

	assert.Equal(t, "Error reading from stdin, error: EOF", err.Error())
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

func Test_two_files_provided(t *testing.T) {
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x00\x10templates/a.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x01" +
		"\x00\x00\x00\x0Bvalues.yaml\x00\x00\x00\x08foo: bar"))

	_, err := ReadAndValidateSources(input)

	// verify that method does not crash and this code is reached
	assert.Nil(t, err)
}

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

func Test_read_byte_as_int(t *testing.T) {
	input, output, _ := os.Pipe()
	output.Write([]byte("\x00\x00\x55\x34"))
	output.Close()
	number, err := readByteAsInt(input)

	assert.NoError(t, err)
	assert.Equal(t, 21812, number)
}
