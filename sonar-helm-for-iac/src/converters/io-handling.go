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
	"bytes"
	"fmt"
	"github.com/samber/mo"
	"io"
	"os"
	"strconv"
	"strings"
)

var END_TOKEN = []byte("END\n")

type InputReader interface {
	// ReadInput
	// Reads from the given scanner expecting the following format:
	// <file name>
	// <number of lines to read>
	// <file content>
	// [<more blocks in the format above>]
	// END
	ReadInput(scanner *bufio.Scanner) (string, Files, error)
}

type StdinReader struct{}

func (s StdinReader) ReadInput(scanner *bufio.Scanner) (string, Files, error) {
	contents := Files{}
	firstLine, err := s.readInput(scanner, 1)
	if err != nil {
		return "", nil, err
	}
	if len(firstLine) == 0 {
		fmt.Fprintf(os.Stderr, "Received empty input, exiting\n")
		return "", contents, nil
	}

	templateName := strings.TrimSuffix(string(firstLine), "\n")
	for !bytes.Equal(firstLine, END_TOKEN) {
		name := strings.TrimSuffix(string(firstLine), "\n")

		var content []byte
		contentResult := mo.TupleToResult(s.readInput(scanner, 1)).FlatMap(
			func(lengthBytes []byte) mo.Result[[]byte] {
				lengthTrimmed := strings.TrimSuffix(string(lengthBytes), "\n")
				length, err := strconv.Atoi(lengthTrimmed)
				if err != nil {
					return mo.Err[[]byte](err)
				}
				fmt.Fprintf(os.Stderr, "Reading %d lines of file %s from stdin\n", length, name)
				if length == 0 {
					// read new line and ignore it. HelmEvaluator writes a single empty line for empty files
					s.readInput(scanner, 1)
					return mo.TupleToResult(make([]byte, 0), nil)
				}
				return mo.TupleToResult(s.readInput(scanner, length))
			})
		if contentResult.IsOk() {
			content = contentResult.MustGet()
		}

		firstLine, err = contentResult.FlatMap(func([]byte) mo.Result[[]byte] {
			return mo.TupleToResult(s.readInput(scanner, 1))
		}).Get()

		if err != nil {
			return "", nil, err
		}

		contents[name] = content
	}
	fmt.Fprintf(os.Stderr, "Received END signal, exiting\n")
	return templateName, contents, nil
}

// readInput
// Reads nLines from the given scanner and returns as a single string.
// If nLines is negative, reads all lines until EOF.
func (s StdinReader) readInput(scanner *bufio.Scanner, nLines int) ([]byte, error) {
	rawInput := make([]byte, 0)
	linesToRead := nLines
	for scanner.Scan() {
		rawInput = append(rawInput, scanner.Bytes()...)
		linesToRead--
		if linesToRead == 0 {
			break
		}
	}

	// if scanner has encountered an error, scanner.Err will return it here
	return rawInput, scanner.Err()
}

// By default bufio.Scanner scans input line by line using \n (LF) as separator, trims \r (CR)
// and doesn't include new line separator in token.
// This fuction split tokens using \r\n or \r or \n or \u2028 or \u2029 and include them in tokens.
func ScanLinesIncludeNewLine(data []byte, atEOF bool) (advance int, token []byte, err error) {
	if atEOF && len(data) == 0 {
		return 0, nil, nil
	}

	indexCRLF := bytes.Index(data, []byte("\r\n"))
	indexNewLine := bytes.IndexAny(data, "\r\n\u2028\u2029")

	if indexCRLF >= 0 && indexCRLF <= indexNewLine {
		// line ends with CRLF
		return indexCRLF + 2, data[0 : indexCRLF+2], nil
	}

	if indexNewLine >= 0 {
		// line ends with CR or LF or \u2028 or \u2029
		return indexNewLine + 1, data[0 : indexNewLine+1], nil
	}

	// If we're at EOF, we have a final, non-terminated line. Return it.
	if atEOF {
		return len(data), data, nil
	}
	// Request more data.
	return 0, nil, nil
}

func CreateScanner(reader io.Reader) *bufio.Scanner {
	scanner := bufio.NewScanner(reader)
	scanner.Split(ScanLinesIncludeNewLine)
	return scanner
}
