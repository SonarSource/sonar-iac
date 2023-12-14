package org_sonarsource_iac_helm

import (
	"bufio"
	"fmt"
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
	ReadInput(scanner *bufio.Scanner) []Content
}

type StdinReader struct{}

func (s StdinReader) ReadInput(scanner *bufio.Scanner) []Content {
	contents := make([]Content, 0)
	firstLine := s.readInput(scanner, 1)
	if firstLine == "" {
		fmt.Fprintf(os.Stderr, "Received empty input, exiting\n")
		return contents
	}
	for firstLine != "END" {
		name := firstLine
		length, _ := strconv.Atoi(s.readInput(scanner, 1))
		fmt.Fprintf(os.Stderr, "Reading %d lines from stdin\n", length)
		content := s.readInput(scanner, length)
		contents = append(contents, Content{Name: name, Content: content})
		firstLine = s.readInput(scanner, 1)
	}
	fmt.Fprintf(os.Stderr, "Received END signal, exiting\n")
	return contents
}

// readInput
// Reads nLines from the given scanner and returns as a single string.
// If nLines is negative, reads all lines until EOF.
func (s StdinReader) readInput(scanner *bufio.Scanner, nLines int) string {
	if nLines == 0 {
		fmt.Fprintf(os.Stderr, "Skipping request to read 0 lines\n")
		return ""
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
	return bytesToString(rawInput)
}

func bytesToString(input [][]byte) string {
	result := make([]string, len(input))
	for i, b := range input {
		result[i] = string(b)
	}
	return strings.Join(result, "\n")
}
