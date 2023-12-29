// SonarQube IaC Plugin
// Copyright (C) 2021-2023 SonarSource SA
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
	"errors"
	"fmt"
	"github.com/samber/mo"
	"os"
	"strconv"
)

var END_TOKEN = []byte("END")

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

	templateName := string(firstLine)
	for !bytes.Equal(firstLine, END_TOKEN) {
		name := string(firstLine)

		var content []byte
		contentResult := mo.TupleToResult(s.readInput(scanner, 1)).FlatMap(
			func(lengthBytes []byte) mo.Result[[]byte] {
				length, err := strconv.Atoi(string(lengthBytes))
				if err != nil {
					return mo.Err[[]byte](err)
				}
				fmt.Fprintf(os.Stderr, "Reading %d lines from stdin\n", length)
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
	if nLines == 0 {
		return nil, errors.New("request to read 0 lines aborted")
	}
	rawInput := make([][]byte, 0)
	linesToRead := nLines
	for scanner.Scan() {
		rawInput = append(rawInput, scanner.Bytes())
		linesToRead--
		if linesToRead == 0 {
			break
		}
	}
	// if scanner has encountered an error, scanner.Err will return it here
	return joinWithDelimiter(rawInput, "\n"), scanner.Err()
}

func joinWithDelimiter(bytes [][]byte, delimiter string) []byte {
	result := make([]byte, 0)
	for i, line := range bytes {
		result = append(result, line...)
		if i != len(bytes)-1 {
			result = append(result, []byte(delimiter)...)
		}
	}
	return result
}
