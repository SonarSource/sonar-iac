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
	"errors"
	"fmt"
	"github.com/samber/mo"
	"os"
	"strconv"
	"strings"
)

type Content struct {
	Name    string
	Content string
}

type InputReader interface {
	// ReadInput
	// Reads from the given scanner expecting the following format:
	// <template name>
	// <number of lines to read>
	// <template content>
	// ...
	// END
	ReadInput(scanner *bufio.Scanner) ([]Content, error)
}

type StdinReader struct{}

func (s StdinReader) ReadInput(scanner *bufio.Scanner) ([]Content, error) {
	contents := make([]Content, 0)
	firstLine, err := s.readInput(scanner, 1)
	if err != nil {
		return nil, err
	}
	if firstLine == "" {
		fmt.Fprintf(os.Stderr, "Received empty input, exiting\n")
		return contents, nil
	}

	for firstLine != "END" {
		name := firstLine

		var content string
		contentResult := mo.TupleToResult(s.readInput(scanner, 1)).FlatMap(
			func(lengthStr string) mo.Result[string] {
				length, err := strconv.Atoi(lengthStr)
				if err != nil {
					return mo.Err[string](err)
				}
				fmt.Fprintf(os.Stderr, "Reading %d lines from stdin\n", length)
				return mo.TupleToResult(s.readInput(scanner, length))
			})
		if contentResult.IsOk() {
			content = contentResult.MustGet()
		}

		firstLine, err = contentResult.FlatMap(func(string) mo.Result[string] {
			return mo.TupleToResult(s.readInput(scanner, 1))
		}).Get()

		if err != nil {
			return nil, err
		}

		contents = append(contents, Content{Name: name, Content: content})
	}
	fmt.Fprintf(os.Stderr, "Received END signal, exiting\n")
	return contents, nil
}

// readInput
// Reads nLines from the given scanner and returns as a single string.
// If nLines is negative, reads all lines until EOF.
func (s StdinReader) readInput(scanner *bufio.Scanner, nLines int) (string, error) {
	if nLines == 0 {
		return "", errors.New("request to read 0 lines aborted")
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
	return bytesToString(rawInput), scanner.Err()
}

func bytesToString(input [][]byte) string {
	result := make([]string, len(input))
	for i, b := range input {
		result[i] = string(b)
	}
	return strings.Join(result, "\n")
}
