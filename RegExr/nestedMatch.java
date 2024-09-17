import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

class nestedMatch {

    // Utility function to create a regex pattern dynamically
    public static String createPattern(int n) {
        return "(a?){" + n + "}(a){" + n + "}";
    }

    // Function to perform regex matching with a timeout
    public static boolean matchWithTimeout(String text, String pattern, long timeout, TimeUnit unit) throws TimeoutException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                Pattern regex = Pattern.compile(pattern);
                return regex.matcher(text).matches();
            }
        });

        try {
            return future.get(timeout, unit);  // Await the result with timeout
        } catch (TimeoutException e) {
            future.cancel(true);  // Cancel the task if timeout occurs
            throw e;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        } finally {
            executor.shutdown();
        }
    }

    // Main function to run the sequence
    public static void runSequentially(int maxTries, long matchTimeout, TimeUnit unit) {
        List<Integer> nValues = new ArrayList<>();
        List<Double> timeValues = new ArrayList<>();

        for (int n = 1; n <= maxTries; n++) {
            StringBuilder textBuilder = new StringBuilder();
            for (int i = 0; i < n; i++) {
                textBuilder.append("a");
            }
            String text = textBuilder.toString();
            String pattern = createPattern(n);

            long startTime = System.nanoTime();

            try {
                boolean match = matchWithTimeout(text, pattern, matchTimeout, unit);
                long endTime = System.nanoTime();
                double timeTaken = (endTime - startTime) / 1_000_000_000.0;

                if (!match) {
                    System.out.printf("No match for n=%d\n", n);
                    break;
                }

                nValues.add(n);
                timeValues.add(timeTaken);
                System.out.printf("Time taken for n=%d: %.10f seconds\n", n, timeTaken);

            } catch (TimeoutException e) {
                long endTime = System.nanoTime();
                double timeTaken = (endTime - startTime) / 1_000_000_000.0;
                System.out.printf("Match for n=%d timed out after %.10f seconds\n", n, timeTaken);
                break;
            }
        }

        exportToCSV("java-run-stats.csv", nValues, timeValues);
    }

    // Function to export data points to a CSV file
    public static void exportToCSV(String filename, List<Integer> nValues, List<Double> timeValues) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.append("n,time-seconds\n");
            for (int i = 0; i < nValues.size(); i++) {
                writer.append(nValues.get(i).toString()).append(",")
                        .append(String.format("%.10f", timeValues.get(i))).append("\n");
            }
            System.out.printf("Data successfully exported to %s\n", filename);
        } catch (IOException e) {
            System.out.println("Error creating file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        long matchTimeout = 10;  // seconds
        int maxTries = 1000;

        runSequentially(maxTries, matchTimeout, TimeUnit.SECONDS);
    }
}
