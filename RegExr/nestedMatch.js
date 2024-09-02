const startTime = Date.now();

// A pattern with nested quantifiers
const pattern = /(a+)+b/;

// A longer string of 'a's
const text = 'a'.repeat(100); // 100 'a's

// Attempt to match the pattern
const match = text.match(pattern);

// End timing
const endTime = Date.now();

// Print the result and the time it took
console.log("Match:", match);
console.log("Time taken:", (endTime - startTime) / 1000, "seconds");
