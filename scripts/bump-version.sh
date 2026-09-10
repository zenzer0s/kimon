#!/usr/bin/env bash
#
# Bump the app version, roll the changelog, commit and tag.
#
#   scripts/bump-version.sh 1.2.0
#
# Then review and:  git push origin master --follow-tags
# The Release workflow then builds a signed APK and opens a draft GitHub release.

set -euo pipefail

new="${1:-}"
if ! [[ "$new" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "usage: $0 <major.minor.patch>   e.g. $0 1.2.0" >&2
    exit 1
fi

cd "$(dirname "$0")/.."

if [[ -n "$(git status --porcelain)" ]]; then
    echo "error: working tree is dirty; commit or stash first." >&2
    exit 1
fi

IFS=. read -r maj min pat <<< "$new"
code=$(( maj * 10000 + min * 100 + pat ))

props="gradle/version.properties"
cur_code=$(grep -E '^VERSION_CODE=' "$props" | cut -d= -f2)
if (( code <= cur_code )); then
    echo "error: new versionCode $code must be greater than current $cur_code" >&2
    exit 1
fi

sed -i.bak -E "s/^VERSION_NAME=.*/VERSION_NAME=$new/" "$props"
sed -i.bak -E "s/^VERSION_CODE=.*/VERSION_CODE=$code/" "$props"
rm -f "$props.bak"

# Turn "## [Unreleased]" into a dated release section, leaving a fresh Unreleased on top.
today=$(date +%F)
tmp=$(mktemp)
awk -v ver="$new" -v date="$today" '
    /^## \[Unreleased\]/ && !done {
        print "## [Unreleased]"
        print ""
        print "## [" ver "] - " date
        done = 1
        next
    }
    { print }
' CHANGELOG.md > "$tmp"
mv "$tmp" CHANGELOG.md

git add "$props" CHANGELOG.md
git commit -m "release: v$new (versionCode $code)"
git tag -a "v$new" -m "v$new"

cat <<EOF

Tagged v$new (versionCode $code).
Review the changelog, then:

    git push origin master --follow-tags

EOF
