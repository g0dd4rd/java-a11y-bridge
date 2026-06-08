#!/bin/bash
# Static analysis for java-a11y-bridge
#
# Compile with system JDK (26), then run analysis tools with JDK 21
# (SpotBugs/PMD need JDK 21 runtime but can analyze JDK 21-target bytecode)

MVN="$HOME/tools/apache-maven-3.9.9/bin/mvn"
JDK21="$HOME/tools/jdk-21.0.2"

if [ ! -d "$JDK21" ]; then
    echo "ERROR: JDK 21 not found at $JDK21"
    echo "Download: curl -sL https://download.java.net/java/GA/jdk21.0.2/.../openjdk-21.0.2_linux-x64_bin.tar.gz | tar xz -C ~/tools/"
    exit 1
fi

echo "=== Compiling with system JDK ($(java -version 2>&1 | head -1)) ==="
$MVN clean compile -q 2>&1 | grep -E "ERROR|FAIL" | grep -v "restricted\|deprecated" | head -5
if [ $? -ne 0 ]; then
    echo "Compile failed — fix errors first"
fi

echo ""
echo "=== ArchUnit (architecture tests) — JDK 26 ==="
$MVN test 2>&1 | grep -E "Tests run|FAIL"

echo ""
echo "=== Checkstyle (code style) — JDK 26 ==="
STYLE=$($MVN checkstyle:check 2>&1 | grep -c "\[ERROR\].*\[")
echo "Total style violations: $STYLE"

echo ""
echo "--- Switching to JDK 21 for bytecode analysis tools ---"
echo ""

echo "=== SpotBugs (bug detection) — JDK 21 ==="
BUGS=$(JAVA_HOME="$JDK21" $MVN spotbugs:check 2>&1 | grep "Total bugs" | grep -oP "\d+" | tail -1)
echo "Total bugs: ${BUGS:-0}"
JAVA_HOME="$JDK21" $MVN spotbugs:check 2>&1 | grep "\[ERROR\]" | grep -oP "\w+$" | sort | uniq -c | sort -rn | head -10

echo ""
echo "=== PMD (best practices / security) — JDK 21 ==="
VIOLATIONS=$(JAVA_HOME="$JDK21" $MVN pmd:check 2>&1 | grep -c "PMD Failure")
echo "Total violations: $VIOLATIONS"
JAVA_HOME="$JDK21" $MVN pmd:check 2>&1 | grep "PMD Failure" | grep -oP "Rule:\w+" | sort | uniq -c | sort -rn | head -10

echo ""
echo "=== OWASP Dependency Check (CVEs) — JDK 26 ==="
$MVN dependency-check:aggregate 2>&1 | grep -E "vulnerab|CVE|Finished|BUILD" | grep -v "in connection" | head -5
echo "Report: target/dependency-check-report.html"

echo ""
echo "Done."
