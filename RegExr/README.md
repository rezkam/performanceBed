# Demonstrating Regex Catastrophic Backtracking

This test bed is designed to illustrate the problem of catastrophic backtracking in regular expressions, which can occur when using patterns with nested quantifiers. The pattern used in this demonstration is (a+)+b, which is intended to match one or more 'a's followed by a 'b'. When presented with a string consisting only of 'a's and no 'b', this pattern can cause some regex engines to enter a state of excessive backtracking, leading to significantly increased execution time or even causing the program to hang.


## Problem Description
Regular expressions are a powerful tool for pattern matching, but they can be prone to performance issues if not used carefully. A classic example of this is when using nested quantifiers, like in the pattern (a+)+b. In this pattern:

- (a+) matches one or more 'a's.
- The outer + allows for multiple occurrences of (a+).
- The pattern is expected to be followed by a 'b'.

When the regex engine tries to match this pattern against a long string of 'a's with no 'b' at the end, it may try numerous combinations of possible matches due to backtracking, which can lead to extremely slow performance.

## Results
| Language   | Time Taken                  |
|------------|-----------------------------|
| Go         | 9.9542e-05 seconds          |
| Java       | 0.003591667 seconds         |
| JavaScript | 90.5432 seconds             |
| Python     | No results after 1000 seconds |
| C# (.NET)  | No results after 1000 seconds |

