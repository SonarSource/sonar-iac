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
