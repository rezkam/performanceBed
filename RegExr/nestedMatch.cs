using System;
using System.Collections.Generic;
using System.IO;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;

// Utility function to create a regex pattern dynamically
string CreatePattern(int n)
{
    return $"(a?){{{n}}}(a){{{n}}}";
}

// Function to perform regex matching with a timeout
async Task<(bool, Exception)> MatchWithTimeoutAsync(string text, string pattern, TimeSpan timeout)
{
    try
    {
        // Run the regex match as a task
        var regexTask = Task.Run(() =>
        {
            Regex regex = new Regex(pattern);
            return regex.IsMatch(text);
        });

        // Wait for either the regex task to complete or the timeout
        if (await Task.WhenAny(regexTask, Task.Delay(timeout)) == regexTask)
        {
            // Regex task completed, return its result
            return (regexTask.Result, null);
        }
        else
        {
            // Timeout
            return (false, new TimeoutException("The operation timed out."));
        }
    }
    catch (Exception ex)
    {
        // Return any exception encountered during the match
        return (false, ex);
    }
}

// Main function to run the sequence of regex matches sequentially, each with a timeout
async Task RunMatchesSequentiallyWithTimeout(int maxTries, TimeSpan matchTimeout)
{
    List<int> nValues = new List<int>();
    List<double> timeValues = new List<double>();

    for (int n = 1; n <= maxTries; n++)
    {
        string text = new string('a', n);
        string pattern = CreatePattern(n);
        DateTime stepStartTime = DateTime.Now;

        (bool match, Exception error) = await MatchWithTimeoutAsync(text, pattern, matchTimeout);
        DateTime stepEndTime = DateTime.Now;
        double timeTaken = (stepEndTime - stepStartTime).TotalSeconds;

        if (error is TimeoutException)
        {
            Console.WriteLine($"Match for n={n} timed out after {timeTaken:F10} seconds");
            break;
        }
        else if (error != null)
        {
            Console.WriteLine($"Error for n={n}: {error.Message}");
            break;
        }

        if (!match)
        {
            Console.WriteLine($"No match for n={n}");
            break;
        }

        nValues.Add(n);
        timeValues.Add(timeTaken);

        Console.WriteLine($"Time taken for n={n}: {timeTaken:F10} seconds");
    }

    ExportToCSV("dotnet-run-stats.csv", nValues, timeValues);
}

// Function to export data points to a CSV file
void ExportToCSV(string filename, List<int> nValues, List<double> timeValues)
{
    using (StreamWriter writer = new StreamWriter(filename))
    {
        writer.WriteLine("n,time-seconds");
        for (int i = 0; i < nValues.Count; i++)
        {
            writer.WriteLine($"{nValues[i]},{timeValues[i]:F10}");
        }
    }

    Console.WriteLine($"Data successfully exported to {filename}");
}

// Entry point of the script
TimeSpan matchTimeout = TimeSpan.FromSeconds(10);
int maxTries = 1000;

// Call the method to run the regex matches sequentially
await RunMatchesSequentiallyWithTimeout(maxTries, matchTimeout);
