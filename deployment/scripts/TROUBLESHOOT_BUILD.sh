#!/bin/bash
# Maven Build Troubleshooting Script
# Run this in Jenkins workspace to diagnose build issues

echo "=========================================="
echo "MAVEN BUILD TROUBLESHOOTING"
echo "=========================================="
echo ""

# Check Maven local repository
echo "📦 Maven Local Repository:"
echo "----------------------------------------"
MVN_REPO="${HOME}/.m2/repository"
if [ -d "$MVN_REPO" ]; then
    echo "Location: $MVN_REPO"
    echo "Size: $(du -sh $MVN_REPO 2>/dev/null | cut -f1)"
    echo "Number of artifacts: $(find $MVN_REPO -name '*.pom' 2>/dev/null | wc -l)"
    echo ""
    echo "Spring Boot artifacts cached:"
    find "$MVN_REPO/org/springframework/boot" -name '*.pom' 2>/dev/null | wc -l | xargs echo "  Found"
else
    echo "❌ Maven repository not found at: $MVN_REPO"
fi
echo ""

# Check for Maven cache issues
echo "🔍 Maven Cache Issues:"
echo "----------------------------------------"
if [ -d "$MVN_REPO" ]; then
    CORRUPTED=$(find "$MVN_REPO" -name "*.lastUpdated" 2>/dev/null | wc -l)
    if [ "$CORRUPTED" -gt 0 ]; then
        echo "⚠️ Found $CORRUPTED corrupted metadata files (*.lastUpdated)"
        echo "   This indicates previous download failures"
        echo "   Fix: rm -rf ~/.m2/repository/*/*.lastUpdated"
    else
        echo "✅ No corrupted metadata files found"
    fi
fi
echo ""

# Check network connectivity to Maven Central
echo "🌐 Network Connectivity:"
echo "----------------------------------------"
echo "Testing Maven Central connectivity..."
if command -v curl > /dev/null; then
    TIME_TOTAL=$(curl -o /dev/null -s -w '%{time_total}' --connect-timeout 10 https://repo1.maven.org 2>/dev/null)
    if [ $? -eq 0 ]; then
        echo "✅ Maven Central reachable (connect time: ${TIME_TOTAL}s)"
    else
        echo "❌ Cannot reach Maven Central"
    fi
else
    echo "⚠️ curl not available - cannot test connectivity"
fi
echo ""

# Check Maven settings
echo "⚙️ Maven Settings:"
echo "----------------------------------------"
if [ -f "$HOME/.m2/settings.xml" ]; then
    echo "Found: $HOME/.m2/settings.xml"
    echo "Content:"
    cat "$HOME/.m2/settings.xml" | head -20
    echo "   (... truncated)"
else
    echo "No custom settings.xml found (using defaults)"
fi
echo ""

# Check if JAR exists from previous build
echo "📋 Previous Build Artifacts:"
echo "----------------------------------------"
if [ -f "target/Week8_Practice1-0.0.1-SNAPSHOT.jar" ]; then
    echo "✅ JAR exists from previous build:"
    ls -lh target/Week8_Practice1-0.0.1-SNAPSHOT.jar
    echo "   Timestamp: $(stat -c %y target/Week8_Practice1-0.0.1-SNAPSHOT.jar 2>/dev/null || stat -f '%Sm' target/Week8_Practice1-0.0.1-SNAPSHOT.jar)"
else
    echo "❌ No JAR file found (needs full build)"
fi
echo ""

# Suggest fixes
echo "=========================================="
echo "RECOMMENDED FIXES:"
echo "=========================================="
echo ""
echo "If build is STILL slow (>10 minutes), try:"
echo ""
echo "1. CLEAN MAVEN CACHE (First time only):"
echo "   rm -rf ~/.m2/repository"
echo "   Then rebuild (will download everything fresh)"
echo ""
echo "2. USE OFFLINE MODE (If deps cached):"
echo "   mvn clean package -o -DskipITs"
echo ""
echo "3. PARALLEL DOWNLOAD (Faster dependency resolution):"
echo "   mvn clean package -T 1C -DskipITs"
echo "   (Uses 1 thread per CPU core)"
echo ""
echo "4. SKIP DEP CHECKUP (Quick build):"
echo "   mvn package -o -Dmaven.test.skip=true"
echo ""
echo "5. PRE-LOAD DEPENDENCIES (One-time setup):"
echo "   mvn dependency:go-offline -B"
echo ""
echo "=========================================="
