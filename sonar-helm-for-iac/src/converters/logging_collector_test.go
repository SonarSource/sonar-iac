// SonarQube IaC Plugin
// Copyright (C) SonarSource Sàrl
// mailto:info AT sonarsource DOT com
//
// You can redistribute and/or modify this program under the terms of
// the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
	"github.com/stretchr/testify/assert"
	"testing"
)

func Test_Logs_Is_Empty_After_Initializing(t *testing.T) {
	loggingCollector := NewDefaultLoggingCollector()

	assert.Equal(t, 0, len(loggingCollector.GetLogs()))
}

func Test_Append_Should_Function_Correctly(t *testing.T) {
	loggingCollector := NewDefaultLoggingCollector()

	loggingCollector.AppendLog("First log")
	loggingCollector.AppendLog("Second log")

	assert.Equal(t, 2, len(loggingCollector.GetLogs()))
	assert.Equal(t, "First log", loggingCollector.GetLogs()[0])
	assert.Equal(t, "Second log", loggingCollector.GetLogs()[1])
}

func Test_Flush_Should_Clear_Logs(t *testing.T) {
	loggingCollector := NewDefaultLoggingCollector()

	loggingCollector.AppendLog("First log")
	loggingCollector.AppendLog("Second log")
	loggingCollector.FlushLogs()

	assert.Equal(t, 0, len(loggingCollector.GetLogs()))
}
