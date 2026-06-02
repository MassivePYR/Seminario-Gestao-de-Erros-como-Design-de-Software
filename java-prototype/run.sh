#!/bin/bash
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
mkdir -p "$BASE_DIR/out"
javac -d "$BASE_DIR/out" "$BASE_DIR/src/main/java/banking/"*.java && \
java -cp "$BASE_DIR/out" banking.Main
