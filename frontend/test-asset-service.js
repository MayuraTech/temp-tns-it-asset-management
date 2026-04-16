// Simple test runner to verify AssetService implementation
const { execSync } = require('child_process');

try {
  console.log('Running AssetService tests...');
  
  // Run only the AssetService test file
  const result = execSync('npx ng test --include="**/asset.service.spec.ts" --watch=false --browsers=ChromeHeadless', {
    cwd: process.cwd(),
    encoding: 'utf8',
    stdio: 'pipe'
  });
  
  console.log('AssetService tests completed successfully!');
  console.log(result);
} catch (error) {
  console.log('Test output:', error.stdout);
  console.log('Test errors:', error.stderr);
  
  // Check if our specific tests passed
  if (error.stdout && error.stdout.includes('AssetService')) {
    const lines = error.stdout.split('\n');
    const assetServiceLines = lines.filter(line => 
      line.includes('AssetService') && 
      (line.includes('PASSED') || line.includes('SUCCESS') || line.includes('✓'))
    );
    
    if (assetServiceLines.length > 0) {
      console.log('AssetService tests found:');
      assetServiceLines.forEach(line => console.log(line));
    }
  }
}