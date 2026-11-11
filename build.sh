#!/bin/bash
# Compile and run the GUI (Linux / macOS)
cd "$(dirname "$0")"
mkdir -p out
javac -d out src/inventory/*.java && java -cp out inventory.Main
# Console version instead:  java -cp out inventory.ConsoleMain
