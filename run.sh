#!/bin/bash
# Scrabble launch script
# This script builds and runs the Scrabble game

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

COMPLETION_FILE="$SCRIPT_DIR/scripts/scrabble-completion.bash"
BASHRC_FILE="$HOME/.bashrc"
SETUP_COMPLETION_SCRIPT="$SCRIPT_DIR/scripts/setup-completion.sh"
COMPLETION_LINE="source \"$COMPLETION_FILE\""

install_completion_if_needed() {
    # Only propose auto-install for interactive Bash sessions with a completion script present.
    if [ ! -t 0 ] || [ -z "${BASH_VERSION:-}" ] || [ ! -f "$COMPLETION_FILE" ]; then
        return
    fi

    if [ ! -f "$BASHRC_FILE" ]; then
        touch "$BASHRC_FILE" 2>/dev/null || return
    fi

    if grep -Fqx "$COMPLETION_LINE" "$BASHRC_FILE"; then
        return
    fi

    echo "Bash completion for ./run.sh is not installed yet."
    printf "Install it automatically in %s now? [Y/n] " "$BASHRC_FILE"
    read -r reply

    case "$reply" in
        ""|"y"|"Y"|"yes"|"YES")
            printf '\n%s\n' "$COMPLETION_LINE" >> "$BASHRC_FILE"
            echo "Completion installed."
            echo "To activate it now in this terminal, run:"
            echo "source \"$SETUP_COMPLETION_SCRIPT\""
            echo "It will be active automatically in new terminals."
            ;;
        *)
            echo "Completion installation skipped."
            ;;
    esac
}

install_completion_if_needed

# Parse launcher-specific options (without consuming application options)
BUILD_VERBOSE=false
APP_ARGS=()
for arg in "$@"; do
    if [ "$arg" = "--build-verbose" ]; then
        BUILD_VERBOSE=true
    else
        APP_ARGS+=("$arg")
    fi
done

# Build the project (with tests)
if [ "$BUILD_VERBOSE" = true ]; then
    echo "Building project with tests..." >&2
    mvn clean package
    if [ $? -ne 0 ]; then
        echo "Error: Build failed." >&2
        exit 1
    fi
else
    echo "Building project with tests..." >&2
    echo "Use --build-verbose to see maven logs." >&2
     mvn clean package
    if [ $? -ne 0 ]; then
        echo "Error: Build failed. Run 'mvn clean package' manually to see errors." >&2
        exit 1
    fi
fi

# Run the packaged application
JAR_FILE=$(find target -maxdepth 1 -name "*.jar" ! -name "original-*.jar" | head -n 1)
if [ -z "$JAR_FILE" ]; then
    echo "Error: no runnable JAR found in target/." >&2
    exit 1
fi

exec java -jar "$JAR_FILE" "${APP_ARGS[@]}"

exit $?