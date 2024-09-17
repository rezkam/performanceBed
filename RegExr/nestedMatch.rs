//! ```cargo
//! edition = "2021"
//! [dependencies]
//! regex = "1"
//! tokio = { version = "1", features = ["full"] }
//! csv = "1"
//! ```

use regex::Regex;
use std::{time::{Duration, Instant}};
use tokio::time::timeout;
use tokio::task;
use csv::Writer;
use std::error::Error;
use std::fs::File;

// Utility function to create a regex pattern dynamically
fn create_pattern(n: usize) -> String {
    format!("(a?){{{}}}(a){{{}}}", n, n)
}

// Asynchronously perform regex matching with a timeout
async fn match_with_timeout(text: String, pattern: String, timeout_duration: Duration) -> Result<bool, Box<dyn Error>> {
    let result = timeout(timeout_duration, task::spawn_blocking(move || {
        let re = Regex::new(&pattern).unwrap();
        re.is_match(&text)
    }))
    .await??;

    Ok(result)
}

// Main function to run the sequence
async fn run_sequentially(max_tries: usize, match_timeout: Duration) -> Result<(), Box<dyn Error>> {
    let mut n_values = vec![];
    let mut time_values = vec![];

    for n in 1..=max_tries {
        // Create the string "a^n" (e.g., "a", "aa", "aaa", ...)
        let text: String = "a".repeat(n);
        let pattern = create_pattern(n);

        let start_time = Instant::now();

        // Perform the regex match with the timeout
        let match_result = match_with_timeout(text.clone(), pattern.clone(), match_timeout).await;

        let time_taken = start_time.elapsed().as_secs_f64();

        match match_result {
            Ok(matched) => {
                if !matched {
                    println!("No match for n={}", n);
                    break;
                }
                println!("Time taken for n={}: {:.10} seconds", n, time_taken);
            }
            Err(_) => {
                println!("Match for n={} timed out after {:.10} seconds", n, time_taken);
                break;
            }
        }

        n_values.push(n);
        time_values.push(time_taken);
    }

    // Export data points to a CSV file
    export_to_csv("rust-run-stats.csv", &n_values, &time_values)?;

    Ok(())
}

// Function to export data points to a CSV file
fn export_to_csv(filename: &str, n_values: &[usize], time_values: &[f64]) -> Result<(), Box<dyn Error>> {
    let file = File::create(filename)?;
    let mut writer = Writer::from_writer(file);

    // Write header
    writer.write_record(&["n", "time-seconds"])?;

    // Write data points
    for (n, time) in n_values.iter().zip(time_values.iter()) {
        writer.write_record(&[n.to_string(), format!("{:.10}", time)])?;
    }

    println!("Data successfully exported to {}", filename);
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    let match_timeout = Duration::from_secs(10);
    let max_tries = 1000;

    run_sequentially(max_tries, match_timeout).await?;

    Ok(())
}
