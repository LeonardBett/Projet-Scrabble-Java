#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_DIR"

# Session-only WSL display setup for GUI launch.
if grep -qi microsoft /proc/version 2>/dev/null; then
  if [[ -z "${WAYLAND_DISPLAY:-}" ]]; then
    if [[ -z "${DISPLAY:-}" ]]; then
      host_ip="$(awk '/nameserver/ {print $2; exit}' /etc/resolv.conf)"
      if [[ -n "$host_ip" ]]; then
        export DISPLAY="$host_ip:0"
      fi
    fi

    if [[ -z "${DISPLAY:-}" ]]; then
      echo "DISPLAY is not set in this WSL session." >&2
      echo "Enable WSLg (recommended) or start an X server on Windows." >&2
      exit 1
    fi

    display_host="${DISPLAY%%:*}"
    display_number="${DISPLAY#*:}"
    display_number="${display_number%%.*}"

    if [[ -n "$display_host" && "$display_number" =~ ^[0-9]+$ ]]; then
      x11_port=$((6000 + display_number))
      if ! timeout 2 bash -lc "cat < /dev/null > /dev/tcp/${display_host}/${x11_port}" 2>/dev/null; then
        echo "X server unreachable at ${display_host}:${x11_port}." >&2
        echo "Start VcXsrv/X410 on Windows, then retry." >&2
        exit 1
      fi
    fi
  fi
fi

exec "$PROJECT_DIR/run.sh" -g "$@"
