const fs = require('fs').promises;
const { Worker, isMainThread, parentPort, workerData } = require('worker_threads');
const { performance } = require('perf_hooks');

// Utility function to generate the text "a^n"
const generateText = (n) => 'a'.repeat(n);

// Utility function to create the regex pattern dynamically
const createPattern = (n) => `(a?){${n}}(a){${n}}`;

// Function to run the regex in a worker thread
function runRegexInWorker(text, pattern) {
  return new Promise((resolve, reject) => {
    const worker = new Worker(__filename, { workerData: { text, pattern } });

    // Resolve the promise if the worker returns a result
    worker.once('message', (result) => resolve(result));

    // Reject if there's an error
    worker.once('error', (error) => reject(error));

    // Reject if the worker stops with an error code
    worker.once('exit', (code) => {
      if (code !== 0) reject(new Error(`Worker stopped with exit code ${code}`));
    });
  });
}

// Main function to run matches sequentially
async function runSequentially(maxTries, matchTimeout) {
  const nValues = [];
  const timeValues = [];

  for (let n = 1; n <= maxTries; n++) {
    const text = generateText(n);
    const pattern = createPattern(n);
    
    console.log(`Running match for n=${n}`);

    const stepStartTime = performance.now();

    try {
      // Wait for either the regex match or timeout
      const match = await Promise.race([
        runRegexInWorker(text, pattern),
        new Promise((_, reject) => setTimeout(() => reject(new Error('Timeout')), matchTimeout * 1000))
      ]);

      const stepEndTime = performance.now();
      const timeTaken = (stepEndTime - stepStartTime) / 1000;

      if (!match) {
        console.log(`No match found for n=${n}`);
        break;
      }

      nValues.push(n);
      timeValues.push(timeTaken);
      console.log(`Time taken for n=${n}: ${timeTaken.toFixed(10)} seconds`);

    } catch (error) {
      if (error.message === 'Timeout') {
        console.log(`Match for n=${n} timed out after ${matchTimeout} seconds`);
      } else {
        console.error(`Error during regex matching for n=${n}:`, error);
      }
      break;
    }
  }

  await exportToCSV('node-run-stats.csv', nValues, timeValues);
}

// Function to export results to CSV
async function exportToCSV(filename, nValues, timeValues) {
  const header = 'n,time-seconds\n';
  const rows = nValues
    .map((n, i) => `${n},${timeValues[i].toFixed(10)}`)
    .join('\n');

  try {
    await fs.writeFile(filename, header + rows);
    console.log(`Data successfully exported to ${filename}`);
  } catch (error) {
    console.error(`Error writing to file: ${error}`);
  }
}

// Worker thread logic to perform regex matching
if (!isMainThread) {
  const { text, pattern } = workerData;
  const regex = new RegExp(pattern);
  const match = regex.test(text);
  parentPort.postMessage(match);
}

// Main execution
if (isMainThread) {
  const main = async () => {
    const matchTimeout = 10; // in seconds
    const maxTries = 1000;

    await runSequentially(maxTries, matchTimeout);
  };

  main().catch((error) => {
    console.error(`An error occurred: ${error}`);
  });
}
