import re
import time

# A pattern with nested quantifiers
pattern = re.compile(r"(a+)+b")

# A longer string of 'a's
text = "a" * 100  # 100 'a's

# Start timing
start_time = time.time()

# Attempt to match the pattern
match = pattern.match(text)

# End timing
end_time = time.time()

# Print the result and the time it took
print("Match:", match)
print("Time taken:", end_time - start_time, "seconds")
