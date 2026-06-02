#!/bin/bash
# Instale o Rust antes: curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$BASE_DIR" && cargo run
