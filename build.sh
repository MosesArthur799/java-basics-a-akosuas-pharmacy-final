#!/bin/sh
set -eu

root=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
classes="$root/target/classes"
test_classes="$root/target/test-classes"

mkdir -p "$classes" "$test_classes"
javac -d "$classes" "$root"/src/main/java/*.java
javac -d "$test_classes" -cp "$classes" "$root/src/test/java/PharmacyTest.java"
