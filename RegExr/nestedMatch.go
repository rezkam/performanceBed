package main

import (
	"fmt"
	"regexp"
	"time"
)

func main() {
	startTime := time.Now()

	// A pattern with nested quantifiers
	pattern := "(a+)+b"
	re := regexp.MustCompile(pattern)

	// A longer string of 'a's
	text := "a"
	for i := 0; i < 100; i++ {
		text += "a"
	}

	// Attempt to match the pattern
	match := re.FindString(text)

	// End timing
	endTime := time.Now()

	// Print the result and the time it took
	fmt.Println("Match:", match)
	fmt.Println("Time taken:", endTime.Sub(startTime).Seconds(), "seconds")
}
