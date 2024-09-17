import re
import time
import csv
from multiprocessing import Process, Queue

# Utility function to generate the text "a^n"
def generate_text(n):
    return 'a' * n

# Utility function to create the regex pattern dynamically
def create_pattern(n):
    return r'(a?){{{}}}(a){{{}}}'.format(n, n)

# Function to perform the regex match in a separate process
def run_regex_in_process(queue, text, pattern):
    regex = re.compile(pattern)
    match = bool(regex.fullmatch(text))  # Match the entire string
    queue.put(match)  # Send result back via the queue

# Function to run the regex with a timeout using multiprocessing
def run_regex_with_timeout(text, pattern, timeout):
    queue = Queue()
    process = Process(target=run_regex_in_process, args=(queue, text, pattern))
    process.start()
    
    process.join(timeout=timeout)  # Wait for the process to finish within the timeout

    if process.is_alive():
        process.terminate()  # Kill the process if it exceeds the timeout
        process.join()  # Ensure the process is cleaned up
        return None  # Indicate a timeout occurred
    else:
        return queue.get()  # Return the result if the process finished

# Function to export data points to a CSV file
def export_to_csv(filename, n_values, time_values):
    with open(filename, mode='w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(['n', 'time-seconds'])
        writer.writerows(zip(n_values, [f"{t:.10f}" for t in time_values]))

    print(f"Data successfully exported to {filename}")

# Function to run the sequence of regex patterns sequentially with a timeout for each
def run_sequentially(max_tries, match_timeout, output_file):
    n_values = []
    time_values = []

    for n in range(1, max_tries + 1):
        text = generate_text(n)
        pattern = create_pattern(n)

        print(f'Running match for n={n}')

        start_time = time.time()
        match = run_regex_with_timeout(text, pattern, match_timeout)
        end_time = time.time()
        time_taken = end_time - start_time

        if match is None:
            print(f"Match for n={n} timed out after {match_timeout} seconds")
            break
        elif match:
            print(f"Match found for n={n} in {time_taken:.10f} seconds")
            n_values.append(n)
            time_values.append(time_taken)
        else:
            print(f"No match found for n={n}")
            break

    export_to_csv(output_file, n_values, time_values)

if __name__ == "__main__":
    match_timeout = 5  # Timeout in seconds for each pattern match
    max_tries = 100  # Maximum number of patterns to try
    output_file = 'python-run-stats.csv'  # Output file

    run_sequentially(max_tries, match_timeout, output_file)
