#!/bin/bash
# Scrabble launch script
# This script builds and runs the Scrabble game

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# Check if verbose mode is requested
VERBOSE=false
if [[ "$*" == *"-v"* ]]; then
    VERBOSE=true
fi

# Build the project
if [ "$VERBOSE" = true ]; then
    echo "Building project..." >&2
    mvn clean install -Djacoco.skip=true
    if [ $? -ne 0 ]; then
        echo "Error: Build failed." >&2
        exit 1
    fi
else
    echo "Building project..." >&2
    echo "Use -v to see maven logs." >&2
    mvn clean install -Djacoco.skip=true >/dev/null 2>&1
    if [ $? -ne 0 ]; then
        echo "Error: Build failed. Run 'mvn clean install' manually to see errors." >&2
        exit 1
    fi
fi

# Filter out the -v flag before passing args to the application
APP_ARGS=()
for arg in "$@"; do
    if [ "$arg" != "-v" ]; then
        APP_ARGS+=("$arg")
    fi
done

# Run the application
if [ "$VERBOSE" = true ]; then
    mvn exec:java -Dexec.mainClass="fr.u_bordeaux.scrabble.App" -Dexec.args="${APP_ARGS[*]}"
else
    mvn exec:java -Dexec.mainClass="fr.u_bordeaux.scrabble.App" -Dexec.args="${APP_ARGS[*]}" -q
fi

exit $?