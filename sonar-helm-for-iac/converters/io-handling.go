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

// SourceCode represents a source file which is passed from the Helm evaluator.
// Can be template itself, values.yaml or any imported file.
type SourceCode struct {
	Name    string
	Content string
}

type InputReader interface {
	// ReadInput
	// Reads from the given scanner expecting the following format:
	// <file name>
	// <number of lines to read>
	// <file content>
	// [<more blocks in the format above>]
	// END
	ReadInput(scanner *bufio.Scanner) ([]SourceCode, error)
}

type StdinReader struct{}

func FlatMap[T, V any](mapper func(T) mo.Result[V], result mo.Result[T]) mo.Result[V] {
	if result.IsOk() {
		return mapper(result.MustGet())
	}
	return mo.Err[V](result.Error())
}

func Map[T, V any](mapper func(T) (V, error), result mo.Result[T]) mo.Result[V] {
	if result.IsOk() {
		return mo.TupleToResult(mapper(result.MustGet()))
	}
	return mo.Err[V](result.Error())
}

func (s StdinReader) ReadInput(scanner *bufio.Scanner) ([]SourceCode, error) {
	contents := make([]SourceCode, 0)
	firstLine := s.readInput(scanner, 1)
	if firstLine.IsError() {
		return nil, firstLine.Error()
	}
	if firstLine.MustGet() == "" {
		fmt.Fprintf(os.Stderr, "Received empty input, exiting\n")
		return contents, nil
	}

	for firstLine.MustGet() != "END" {
		name := firstLine.MustGet()

		var content string
		contentResult :=
			FlatMap(
				func(length int) mo.Result[string] {
					fmt.Fprintf(os.Stderr, "Reading %d lines from stdin\n", length)
					return s.readInput(scanner, length)
				},
				Map(strconv.Atoi,
					s.readInput(scanner, 1)))
		if contentResult.IsOk() {
			content = contentResult.MustGet()
		}

		firstLine = contentResult.FlatMap(func(string) mo.Result[string] {
			return s.readInput(scanner, 1)
		})

		if firstLine.IsError() {
			return nil, firstLine.Error()
		}

		contents = append(contents, SourceCode{Name: name, Content: content})
	}
	fmt.Fprintf(os.Stderr, "Received END signal, exiting\n")
	return contents, nil
}

// readInput
// Reads nLines from the given scanner and returns as a single string.
// If nLines is negative, reads all lines until EOF.
func (s StdinReader) readInput(scanner *bufio.Scanner, nLines int) mo.Result[string] {
	if nLines == 0 {
		return mo.Err[string](errors.New("request to read 0 lines aborted"))
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
	return Map[[][]byte, string](bytesToString, mo.TupleToResult(rawInput, scanner.Err()))
}

func bytesToString(input [][]byte) (string, error) {
	result := make([]string, len(input))
	for i, b := range input {
		result[i] = string(b)
	}
	return strings.Join(result, "\n"), nil
}
