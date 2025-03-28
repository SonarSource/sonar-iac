package converters

import (
	"fmt"
	"os"
)

type LoggingCollector interface {
	GetLogs() []string
	AppendLog(string)
	FlushLogs()
}

type defaultLoggingCollector struct {
	Logs []string
}

func NewDefaultLoggingCollector() LoggingCollector {
	return &defaultLoggingCollector{[]string{}}
}

func (l *defaultLoggingCollector) GetLogs() []string {
	return l.Logs
}

func (l *defaultLoggingCollector) AppendLog(log string) {
	l.Logs = append(l.Logs, log)
}

// Flush writes all collected messages to stderr, so we log them as DEBUG
func (l *defaultLoggingCollector) FlushLogs() {
	_, _ = fmt.Fprintln(os.Stderr, "Exception encountered, printing recorded logs")
	for _, message := range l.Logs {
		_, err := fmt.Fprintf(os.Stderr, "  %s", message)
		if err != nil {
			break
		}
	}
	_, _ = fmt.Fprintln(os.Stderr, "End of recorded logs")
	// Clear the messages after flushing
	l.Logs = []string{}
}
