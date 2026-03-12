#!/bin/bash
# Scrabble test script
# Runs: clean → build → tests → jacoco → javadoc

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# Check if verbose mode is requested
VERBOSE=false
if [[ "$*" == *"-v"* ]]; then
    VERBOSE=true
fi

echo "Running: clean → build → tests → jacoco → javadoc..." >&2

if [ "$VERBOSE" = true ]; then
    mvn clean verify javadoc:javadoc
else
    echo "Use -v to see maven logs." >&2
    mvn clean verify javadoc:javadoc 2>&1 | tee /tmp/scrabble_test_output.txt > /dev/null

    # Extract and display test summary
    grep -E "Tests run:|BUILD|ERROR" /tmp/scrabble_test_output.txt
fi

EXIT_CODE=${PIPESTATUS[0]}

if [ "$VERBOSE" = false ]; then
    if grep -q "BUILD SUCCESS" /tmp/scrabble_test_output.txt; then
        echo ""
        echo "✅ Build, tests, jacoco et javadoc OK — safe to push!" >&2
        exit 0
    else
        echo ""
        echo "❌ Quelque chose a échoué — do NOT push!" >&2
        echo "Run ./test.sh -v to see details." >&2
        exit 1
    fi
fi

exit $EXIT_CODE