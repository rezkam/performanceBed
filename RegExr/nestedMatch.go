package main

import (
	"context"
	"encoding/csv"
	"fmt"
	"os"
	"regexp"
	"strconv"
	"time"
)

// Utility function to create a regex pattern dynamically
func createPattern(n int) string {
	return "(a?){" + strconv.Itoa(n) + "}(a){" + strconv.Itoa(n) + "}"
}

// Function to perform regex matching with a timeout
func matchWithTimeout(ctx context.Context, text, pattern string) (bool, error) {
	resultChan := make(chan bool)

	// Run the regex match in a goroutine
	go func() {
		re := regexp.MustCompile(pattern)
		match := re.MatchString(text)
		resultChan <- match
	}()

	// Wait for either the match result or the context timeout
	select {
	case <-ctx.Done():
		return false, ctx.Err() // Timeout or cancellation
	case result := <-resultChan:
		return result, nil // Successfully matched or didn't match
	}
}

// Main function to run the sequence
func runSequentially(maxTries int, matchTimeout time.Duration) {

	// Slices to store data points
	var nValues []int
	var timeValues []float64

	for n := 1; n <= maxTries; n++ {
		// Create the string "a^n" (e.g., "a", "aa", "aaa", ...)
		text := ""
		for i := 0; i < n; i++ {
			text += "a"
		}

		pattern := createPattern(n)
		stepStartTime := time.Now()

		// Create a context with a timeout for the match
		ctx, cancel := context.WithTimeout(context.Background(), matchTimeout)
		defer cancel()

		// Perform the regex match with the timeout
		match, err := matchWithTimeout(ctx, text, pattern)
		stepEndTime := time.Now()
		timeTaken := stepEndTime.Sub(stepStartTime).Seconds()

		// Handle the timeout case
		if err == context.DeadlineExceeded {
			fmt.Printf("Match for n=%d timed out after %.10f seconds\n", n, timeTaken)
			break
		} else if err != nil {
			fmt.Printf("Error for n=%d: %v\n", n, err)
			break
		}

		// If no match is found, stop
		if !match {
			fmt.Printf("No match for n=%d\n", n)
			break
		}

		// Save data point
		nValues = append(nValues, n)
		timeValues = append(timeValues, timeTaken)

		fmt.Printf("Time taken for n=%d: %.10f seconds\n", n, timeTaken)
	}

	// Export data points to a CSV file
	exportToCSV("go-run-stats.csv", nValues, timeValues)
}

// Function to export data points to a CSV file
func exportToCSV(filename string, nValues []int, timeValues []float64) {
	file, err := os.Create(filename)
	if err != nil {
		fmt.Println("Error creating file:", err)
		return
	}
	defer file.Close()

	writer := csv.NewWriter(file)
	defer writer.Flush()

	// Write header
	writer.Write([]string{"n", "time-seconds"})

	// Write data points
	for i := range nValues {
		writer.Write([]string{strconv.Itoa(nValues[i]), fmt.Sprintf("%.10f", timeValues[i])})
	}

	fmt.Printf("Data successfully exported to %s\n", filename)
}

func main() {
	matchTimeout := 10 * time.Second
	maxTries := 1000

	runSequentially(maxTries, matchTimeout)
}
