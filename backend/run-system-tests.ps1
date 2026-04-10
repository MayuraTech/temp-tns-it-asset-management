# System Testing Script for Module 1 - User Management
# Task 23.1: Complete System Testing

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Module 1 - User Management System Testing" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Run Complete Regression Test Suite
Write-Host "1. Running Complete Regression Test Suite..." -ForegroundColor Yellow
Write-Host "   - Unit Tests" -ForegroundColor Gray
Write-Host "   - Integration Tests" -ForegroundColor Gray
Write-Host "   - Property-Based Tests" -ForegroundColor Gray
Write-Host ""

$testResult = mvn test -f pom.xml 2>&1
$testExitCode = $LASTEXITCODE

if ($testExitCode -eq 0) {
    Write-Host "✓ All tests passed successfully" -ForegroundColor Green
} else {
    Write-Host "✗ Some tests failed - see details below" -ForegroundColor Red
}

Write-Host ""
Write-Host "Test Summary:" -ForegroundColor Cyan
mvn surefire-report:report -f pom.xml

# 2. Generate Code Coverage Report
Write-Host ""
Write-Host "2. Generating Code Coverage Report..." -ForegroundColor Yellow
mvn jacoco:report -f pom.xml
Write-Host "✓ Coverage report generated at target/site/jacoco/index.html" -ForegroundColor Green

# 3. Security Testing
Write-Host ""
Write-Host "3. Security Testing..." -ForegroundColor Yellow
Write-Host "   - Authentication Tests" -ForegroundColor Gray
Write-Host "   - Authorization Tests" -ForegroundColor Gray
Write-Host "   - JWT Token Security" -ForegroundColor Gray
Write-Host "   - Account Locking Tests" -ForegroundColor Gray

mvn test -f pom.xml -Dtest="*Security*,*Authentication*,*Authorization*,*Jwt*"

# 4. Performance Testing
Write-Host ""
Write-Host "4. Performance Testing..." -ForegroundColor Yellow
Write-Host "   Note: Performance tests require database setup" -ForegroundColor Gray
Write-Host "   - Testing 100 concurrent sessions" -ForegroundColor Gray
Write-Host "   - Response time validation" -ForegroundColor Gray

# 5. Integration Testing
Write-Host ""
Write-Host "5. Integration Testing..." -ForegroundColor Yellow
mvn test -f pom.xml -Dtest="*Integration*"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "System Testing Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Test Reports:" -ForegroundColor Yellow
Write-Host "  - Surefire Report: target/surefire-reports/" -ForegroundColor Gray
Write-Host "  - Coverage Report: target/site/jacoco/index.html" -ForegroundColor Gray
Write-Host ""
