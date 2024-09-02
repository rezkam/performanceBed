using System;
using System.Text.RegularExpressions;

// Regular expression pattern to cause backtracking issues
string pattern = @"(a+)+b";
string input = new string('a', 100);

// Create a Regex object
Regex regex = new Regex(pattern);

// Measure the time taken to perform the match
var stopwatch = new System.Diagnostics.Stopwatch();
stopwatch.Start();

// Perform the match
var match = regex.Match(input);

stopwatch.Stop();

// Output the results
Console.WriteLine($"Match: {match.Success}");
Console.WriteLine($"Time taken: {stopwatch.Elapsed.TotalSeconds} seconds");
