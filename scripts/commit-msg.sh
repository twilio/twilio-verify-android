#!/bin/sh
echo "Validating commit message convention"

commit_regex='/^(docs|fix|feat|chore|style|refactor|perf|test|BREAKING CHANGE)(?:\((.*)\))?(!?)\: (.*)/'
if ! grep -iqE "$commit_regex" "$1"; then
    echo "*********************************************"
    echo "      Please use conventional commits        "
    echo "          Use structural elements:           "
    echo "     docs|fix|feat|chore|style|refactor      "
    echo "          perf|BREAKING CHANGE               "
    echo "*********************************************"
    exit 1
fi