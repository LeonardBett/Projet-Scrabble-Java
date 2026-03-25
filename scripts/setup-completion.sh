#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPLETION_FILE="$ROOT_DIR/scripts/scrabble-completion.bash"
BASHRC_FILE="$HOME/.bashrc"
COMPLETION_LINE="source \"$COMPLETION_FILE\""

is_sourced() {
  [[ "${BASH_SOURCE[0]}" != "$0" ]]
}

ensure_persistent_completion() {
  if [ ! -f "$COMPLETION_FILE" ]; then
    echo "Completion file not found: $COMPLETION_FILE" >&2
    return 1
  fi

  if [ ! -f "$BASHRC_FILE" ]; then
    touch "$BASHRC_FILE" 2>/dev/null || {
      echo "Cannot create $BASHRC_FILE" >&2
      return 1
    }
  fi

  if ! grep -Fqx "$COMPLETION_LINE" "$BASHRC_FILE"; then
    printf '\n%s\n' "$COMPLETION_LINE" >> "$BASHRC_FILE"
    echo "Completion persistence installed in $BASHRC_FILE"
  else
    echo "Completion persistence already configured in $BASHRC_FILE"
  fi
}

activate_current_shell() {
  # Must be sourced by a Bash shell to affect current completion state.
  if [ -z "${BASH_VERSION:-}" ]; then
    echo "Current shell is not Bash. Open Bash and run: source scripts/setup-completion.sh" >&2
    return 0
  fi

  # shellcheck source=/dev/null
  source "$COMPLETION_FILE"
  echo "Completion activated in current shell."
}

ensure_persistent_completion || {
  if is_sourced; then
    return 1
  fi
  exit 1
}

if is_sourced; then
  activate_current_shell
else
  echo "For immediate activation in this terminal, run:"
  echo "source \"$ROOT_DIR/scripts/setup-completion.sh\""
fi
