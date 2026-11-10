#!/bin/sh
set -eu

root=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
classes="$root/target/classes"
test_classes="$root/target/test-classes"

sh "$root/build.sh"
java -cp "$classes:$test_classes" PharmacyTest
