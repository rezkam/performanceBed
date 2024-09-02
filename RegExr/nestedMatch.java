import java.util.regex.Pattern;
import java.util.regex.Matcher;

class RegexBacktracking {
    public static void main(String[] args) {
        long startTime = System.nanoTime();

        // A pattern with nested quantifiers
        String pattern = "(a+)+b";
        Pattern re = Pattern.compile(pattern);

        // A longer string of 'a's with a 'b' at the end, then remove the 'b'
        StringBuilder text = new StringBuilder("a");
        for (int i = 0; i < 100; i++) {
            text.append("a");
        }
        text.append("b");

        // Remove the 'b' to trigger backtracking
        String testString = text.toString().substring(0, text.length() - 1);

        // Attempt to match the pattern
        Matcher matcher = re.matcher(testString);
        boolean match = matcher.find();

        // End timing
        long endTime = System.nanoTime();

        // Print the result and the time it took
        System.out.println("Match: " + match);
        System.out.println("Time taken: " + (endTime - startTime) / 1e9 + " seconds");
    }
}
