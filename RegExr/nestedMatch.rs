extern crate regex;

use regex::Regex;
use std::time::Instant;

fn main() {
    // Define the pattern
    let pattern = r"(a+)+b";
    let re = Regex::new(pattern).unwrap();

    // Create a string of 100 'a's
    let text = "a".repeat(100);

    // Start timing
    let start_time = Instant::now();

    // Attempt to match the pattern
    let is_match = re.is_match(&text);

    // End timing
    let duration = start_time.elapsed();

    // Print the result and the time it took
    println!("Match: {}", is_match);
    println!("Time taken: {:.6} seconds", duration.as_secs_f64());
}

// cargo script nestedMatch.rs --dep regex="1"