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
	"os"
	"strings"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

func Test_read_single_template(t *testing.T) {
	input, output, _ := os.Pipe()
	_, outputErr := output.Write([]byte("\x00\x00\x00\x0Dtemplate.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x00"))
	loggingTestCollector := NewDefaultLoggingCollector()
	templateName, contents, inputErr := ReadInput(input, &loggingTestCollector)

	assert.NoError(t, outputErr)
	assert.NoError(t, inputErr)
	assert.Equal(t, "template.yaml", templateName)
	assert.Equal(t, 1, len(contents))
	assert.Equal(t, "apiVersion: v1", contents.Get("template.yaml"))
	assert.Equal(t, 1, len(loggingTestCollector.GetLogs()))
	assert.Equal(t, "Reading 14 bytes of file template.yaml from stdin\n", loggingTestCollector.GetLogs()[0])
}

func Test_read_long_template_name(t *testing.T) {
	filename := strings.Repeat("/very/long/path", 100) + "/template.yaml"
	input, output, _ := os.Pipe()
	_, outputErr := output.Write([]byte("\x00\x00\x05\xEA" + filename + "\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x00"))
	loggingTestCollector := NewDefaultLoggingCollector()
	templateName, contents, inputErr := ReadInput(input, &loggingTestCollector)

	assert.NoError(t, outputErr)
	assert.NoError(t, inputErr)
	assert.Equal(t, filename, templateName)
	assert.Equal(t, 1, len(contents))
	assert.Equal(t, "apiVersion: v1", contents.Get(filename))
	assert.Equal(t, 1, len(loggingTestCollector.GetLogs()))
}

func Test_read_template_and_empty_values(t *testing.T) {
	input, output, _ := os.Pipe()
	_, outputErr := output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x01\x00\x00\x00\x0Bvalues.yaml\x00\x00\x00\x00"))
	loggingTestCollector := NewDefaultLoggingCollector()
	templateName, contents, inputErr := ReadInput(input, &loggingTestCollector)

	assert.NoError(t, outputErr)
	assert.NoError(t, inputErr)
	assert.Equal(t, "foo.yaml", templateName)
	assert.Equal(t, 2, len(contents))
	assert.Equal(t, "apiVersion: v1", contents.Get("foo.yaml"))
	assert.Equal(t, "", contents.Get("values.yaml"))
	assert.Equal(t, 2, len(loggingTestCollector.GetLogs()))
}

func Test_read_with_empty_input(t *testing.T) {
	timeout := time.After(1 * time.Second)
	done := make(chan bool)
	loggingTestCollector := NewDefaultLoggingCollector()
	go func() {
		input, output, _ := os.Pipe()
		_, err := output.Write([]byte(""))
		assert.NoError(t, err)
		_, _, err = ReadInput(input, &loggingTestCollector)
		assert.NoError(t, err)
		done <- true
	}()

	select {
	case <-done:
		t.Fatal("It should be timeout for empty input")
	case <-timeout:
	}
	assert.Equal(t, 0, len(loggingTestCollector.GetLogs()))
}

func Test_read_n_lines_from_input(t *testing.T) {
	input, output, _ := os.Pipe()
	_, _ = output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x01" +
		"\x00\x00\x00\x0Bvalues.yaml\x00\x00\x00\x11line1\nline2\nline3"))
	loggingTestCollector := NewDefaultLoggingCollector()
	_, contents, err := ReadInput(input, &loggingTestCollector)

	assert.NoError(t, err)
	assert.Equal(t, 2, len(contents))
	assert.Equal(t, "line1\nline2\nline3", contents.Get("values.yaml"))
	assert.Equal(t, 2, len(loggingTestCollector.GetLogs()))
}

func Test_read_all_lines_from_input_different_new_lines(t *testing.T) {
	// the \u2028 character uses 3 bytes
	content := "line1\r\nline2\nline3\rline4\u2028line5\u2029line6"
	input, output, _ := os.Pipe()
	_, _ = output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x01" +
		"\x00\x00\x00\x0Bvalues.yaml\x00\x00\x00\x28" + content))
	loggingTestCollector := NewDefaultLoggingCollector()
	_, contents, err := ReadInput(input, &loggingTestCollector)

	assert.NoError(t, err)
	assert.Equal(t, 2, len(contents))
	assert.Equal(t, content, contents.Get("values.yaml"))
	assert.Equal(t, 2, len(loggingTestCollector.GetLogs()))
}

func Test_read_one_file_with_trailing_newline(t *testing.T) {
	content := "line1\n"
	input, output, _ := os.Pipe()
	_, _ = output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x01" +
		"\x00\x00\x00\x0Bvalues.yaml\x00\x00\x00\x06" + content))
	loggingTestCollector := NewDefaultLoggingCollector()
	_, contents, err := ReadInput(input, &loggingTestCollector)

	assert.NoError(t, err)
	assert.Equal(t, 2, len(contents))
	assert.Equal(t, content, contents.Get("values.yaml"))
	assert.Equal(t, 2, len(loggingTestCollector.GetLogs()))
}

func Test_read_three_files(t *testing.T) {
	input, output, _ := os.Pipe()
	_, _ = output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x03" +
		"\x00\x00\x00\x0Bvalues.yaml\x00\x00\x00\x0Bline1\nline2" +
		"\x00\x00\x00\x12templates/foo.yaml\x00\x00\x00\x05line3" +
		"\x00\x00\x00\x16templates/_helpers.tpl\x00\x00\x00\x08\nline4\r\n"))
	loggingTestCollector := NewDefaultLoggingCollector()
	_, contents, err := ReadInput(input, &loggingTestCollector)

	assert.NoError(t, err)
	assert.Equal(t, 4, len(contents))
	assert.Equal(t, "line1\nline2", contents.Get("values.yaml"))
	assert.Equal(t, "line3", contents.Get("templates/foo.yaml"))
	assert.Equal(t, "\nline4\r\n", contents.Get("templates/_helpers.tpl"))
	assert.Equal(t, 4, len(loggingTestCollector.GetLogs()))
}

func Test_read_error_handling(t *testing.T) {
	input, output, _ := os.Pipe()
	_, _ = output.Write([]byte("\x00"))
	_ = output.Close()
	loggingTestCollector := NewDefaultLoggingCollector()
	_, _, err := ReadInput(input, &loggingTestCollector)

	assert.Equal(t, "Error reading from stdin, expecting to read 4 bytes, but got 1", err.Error())
	assert.Equal(t, 0, len(loggingTestCollector.GetLogs()))
}

func Test_read_error_handling_2(t *testing.T) {
	input, output, _ := os.Pipe()
	_, _ = output.Write([]byte("\x00\x00\x00\x11foo"))
	_ = output.Close()
	loggingTestCollector := NewDefaultLoggingCollector()
	_, _, err := ReadInput(input, &loggingTestCollector)

	assert.Equal(t, "Error reading from stdin, expecting to read 17 bytes, but got 3", err.Error())
	assert.Equal(t, 0, len(loggingTestCollector.GetLogs()))
}

func Test_read_error_handling_3(t *testing.T) {
	input, output, _ := os.Pipe()
	_, _ = output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00"))
	_ = output.Close()
	loggingTestCollector := NewDefaultLoggingCollector()
	_, _, err := ReadInput(input, &loggingTestCollector)

	assert.Equal(t, "Error reading from stdin, expecting to read 4 bytes, but got 2", err.Error())
	assert.Equal(t, 0, len(loggingTestCollector.GetLogs()))
}

func Test_read_error_handling_4(t *testing.T) {
	input, output, _ := os.Pipe()
	_, _ = output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersi"))
	_ = output.Close()
	loggingTestCollector := NewDefaultLoggingCollector()
	_, _, err := ReadInput(input, &loggingTestCollector)

	assert.Equal(t, "Error reading from stdin, expecting to read 14 bytes, but got 8", err.Error())
	assert.Equal(t, 1, len(loggingTestCollector.GetLogs()))
}

func Test_read_error_handling_5(t *testing.T) {
	input, output, _ := os.Pipe()
	_, _ = output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00"))
	_ = output.Close()
	loggingTestCollector := NewDefaultLoggingCollector()
	_, _, err := ReadInput(input, &loggingTestCollector)

	assert.Equal(t, "Error reading from stdin, expecting to read 4 bytes, but got 1", err.Error())
	assert.Equal(t, 1, len(loggingTestCollector.GetLogs()))
}

func Test_read_error_handling_6(t *testing.T) {
	input, output, _ := os.Pipe()
	_, _ = output.Write([]byte("\x00\x00\x00\x08foo.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x03"))
	_ = output.Close()
	loggingTestCollector := NewDefaultLoggingCollector()
	_, _, err := ReadInput(input, &loggingTestCollector)

	assert.Equal(t, "Error reading from stdin, error: EOF", err.Error())
	assert.Equal(t, 1, len(loggingTestCollector.GetLogs()))
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
	_, _ = output.Write([]byte("\x00\x00\x00\x10templates/a.yaml\x00\x00\x00\x0EapiVersion: v1\x00\x00\x00\x01" +
		"\x00\x00\x00\x0Bvalues.yaml\x00\x00\x00\x08foo: bar"))
	loggingTestCollector := NewDefaultLoggingCollector()
	_, err := ReadAndValidateSources(input, &loggingTestCollector)

	// verify that method does not crash and this code is reached
	assert.Nil(t, err)
	assert.Equal(t, 2, len(loggingTestCollector.GetLogs()))
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
	_, _ = output.Write([]byte("\x00\x00\x55\x34"))
	_ = output.Close()
	number, err := readBytesAsInt(input)

	assert.NoError(t, err)
	assert.Equal(t, 21812, number)
}
