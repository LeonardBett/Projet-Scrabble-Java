# Bash completion for Scrabble launcher options.
# Usage:
#   source scripts/scrabble-completion.bash
#   complete -F _scrabble_run_completion ./run.sh
# Optional (persistent):
#   echo 'source /absolute/path/to/scrabble-java/scripts/scrabble-completion.bash' >> ~/.bashrc
#   echo 'complete -F _scrabble_run_completion ./run.sh' >> ~/.bashrc

_scrabble_contains_word() {
  local needle="$1"
  shift
  local item
  for item in "$@"; do
    if [[ "$item" == "$needle" ]]; then
      return 0
    fi
  done
  return 1
}

_scrabble_run_completion() {
  local cur prev
  cur="${COMP_WORDS[COMP_CWORD]}"
  prev="${COMP_WORDS[COMP_CWORD-1]}"

  # Suggest values expected by options that take arguments.
  case "$prev" in
    -p|--players)
      COMPREPLY=( $(compgen -W "2 3 4" -- "$cur") )
      return 0
      ;;
    -l|--lang)
      COMPREPLY=( $(compgen -W "en fr" -- "$cur") )
      return 0
      ;;
    -ai-time|--ai-time)
      COMPREPLY=( $(compgen -W "1 2 3 5 10 15 20 30 60" -- "$cur") )
      return 0
      ;;
  esac

  local all_opts
  all_opts="-h --help -V --version -g --gui -p --players -b --blitz -l --lang -ai-time --ai-time -ai-exptiminimax --ai-exptiminimax --ai-ml"

  # If current word looks like an option, offer matching options.
  if [[ "$cur" == -* ]]; then
    COMPREPLY=( $(compgen -W "$all_opts" -- "$cur") )
    return 0
  fi

  # Otherwise suggest options that are not already present.
  local chosen remaining word
  chosen=("${COMP_WORDS[@]}")
  remaining=""
  for word in $all_opts; do
    if ! _scrabble_contains_word "$word" "${chosen[@]}"; then
      remaining+="$word "
    fi
  done

  COMPREPLY=( $(compgen -W "$remaining" -- "$cur") )
  return 0
}

# Register completion for common launch commands.
complete -F _scrabble_run_completion ./run.sh
complete -F _scrabble_run_completion run.sh
complete -F _scrabble_run_completion ./scrabble
complete -F _scrabble_run_completion scrabble
