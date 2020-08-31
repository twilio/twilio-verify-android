#!/bin/sh
echo "Validating commit message convention"
commit_regex='^(docs|fix|feat|chore|refactor|perf|test|style|skip)(?:\((.*)\))?(!?)\: (.*)'
if ! grep -iqE "$commit_regex" "$1"; then
    echo "*********************************************"
    echo "      Please use conventional commits        "
    echo "           Structural elements:              "
    echo "     docs|fix|feat|chore|refactor|perf       "
    echo "            test|style|skip                  "
    echo "*********************************************"
    exit 1
fi