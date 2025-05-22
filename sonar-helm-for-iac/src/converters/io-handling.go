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
	"errors"
	"fmt"
	"os"
)

// Reads from the given os.File expecting the following format:
// N (4 bytes) template name length
// <template name>  (N bytes)
// M (4 bytes) template content length
// <template content> (M bytes)
// P (4 bytes) number of help files
// [P times reapeted:]
// R (4 bytes) file name length
// <file name> (R bytes)
// S (4 bytes) file content length
// <file content> (S bytes)
func ReadInput(input *os.File, loggingCollector *LoggingCollector) (string, Files, error) {
	templateName, contentBytes, err := readSingleFile(input, loggingCollector)
	if err != nil {
		return "", nil, err
	}

	contents := Files{}
	contents[templateName] = contentBytes

	numberOfFiles, err := readBytesAsInt(input)
	if err != nil {
		return "", nil, err
	}

	for i := 0; i < numberOfFiles; i++ {
		filename, content, err := readSingleFile(input, loggingCollector)
		if err != nil {
			return "", nil, err
		}
		contents[filename] = content
	}

	return templateName, contents, err
}

func readSingleFile(input *os.File, loggingCollector *LoggingCollector) (string, []byte, error) {
	filenameLength, err := readBytesAsInt(input)
	if err != nil {
		return "", nil, err
	}

	filenameBytes, _, err := readBytes(input, filenameLength)
	if err != nil {
		return "", nil, err
	}

	filename := string(filenameBytes[:])

	contentLength, err := readBytesAsInt(input)
	if err != nil {
		return "", nil, err
	}

	(*loggingCollector).AppendLog(fmt.Sprintf("Reading %d bytes of file %s from stdin\n", contentLength, filename))
	contentBytes, _, err := readBytes(input, contentLength)
	if err != nil {
		return "", nil, err
	}
	return filename, contentBytes, nil
}

// Calling File.Read(buf) reads sometimes fewer bytes than it is needed so there is a need to read again
// and concatenate the results
func readBytes(input *os.File, contentLength int) ([]byte, int, error) {
	contentBytes := make([]byte, contentLength)
	numberOfBytesRead, err := input.Read(contentBytes)
	if err != nil {
		message := fmt.Sprintf("Error reading from stdin, expecting to read %d bytes, but got %d", contentLength, numberOfBytesRead)
		return nil, numberOfBytesRead, errors.New(message)
	}
	sum := numberOfBytesRead
	if numberOfBytesRead != contentLength {
		bytes, numberOfBytesReadInRecursion, err := readBytes(input, contentLength-numberOfBytesRead)
		sum = sum + numberOfBytesReadInRecursion
		if err != nil {
			message := fmt.Sprintf("Error reading from stdin, expecting to read %d bytes, but got %d", contentLength, numberOfBytesRead)
			return nil, sum, errors.New(message)
		}
		copy(contentBytes[numberOfBytesRead:], bytes)
	}
	return contentBytes, sum, nil
}

func readBytesAsInt(input *os.File) (int, error) {
	numberBytes := make([]byte, 4)
	numberOfBytesRead, err := input.Read(numberBytes)
	if err != nil {
		message := "Error reading from stdin, error: " + err.Error()
		fmt.Fprint(os.Stderr, message)
		return 0, errors.New(message)
	}
	if numberOfBytesRead != 4 {
		message := fmt.Sprintf("Error reading from stdin, expecting to read 4 bytes, but got %d", numberOfBytesRead)
		return 0, errors.New(message)
	}
	number := (int(numberBytes[0]))<<24 +
		(int(numberBytes[1]))<<16 +
		(int(numberBytes[2]))<<8 +
		(int(numberBytes[3]))

	return number, nil
}

func ReadAndValidateSources(input *os.File, loggingCollector *LoggingCollector) (*TemplateSources, error) {
	templateName, sources, err := ReadInput(input, loggingCollector)
	if err != nil {
		return nil, fmt.Errorf("error reading content: %w", err)
	}
	if err = validateInput(sources); err != nil {
		return nil, fmt.Errorf("error validating content: %w", err)
	}

	return NewTemplateSourcesFromRawSources(templateName, sources), nil
}

func validateInput(sources Files) error {
	if len(sources) == 0 {
		return errors.New("no input received")
	}
	return nil
}

func NewTemplateSourcesFromRawSources(templateName string, rawSources Files) *TemplateSources {
	return NewTemplateSources(templateName, rawSources)
}
