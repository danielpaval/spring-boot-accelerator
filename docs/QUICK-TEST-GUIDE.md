# Quick Test Guide for Validation

## Step 1: Start the Application
```bash
.\gradlew bootRun
```

Wait for the application to start (look for "Started SpringBootApiDemoApplication").

## Step 2: Test with PowerShell (No Keycloak Required)

If you don't have Keycloak running, you can test the validation directly by bypassing security temporarily.

### Option A: Test Validation Directly (Quickest)

Run this PowerShell command to test validation:

```powershell
# Test missing name - should get 401 (unauthorized) or 400 (validation error)
Invoke-RestMethod -Uri "http://localhost:8080/api/users" `
  -Method POST `
  -ContentType "application/json" `
  -Body "{}"
```

## Step 3: If You Have Keycloak Running

### Get Admin Token:
```powershell
$tokenResponse = Invoke-RestMethod -Uri "http://localhost:8180/realms/dev/protocol/openid-connect/token" `
  -Method POST `
  -ContentType "application/x-www-form-urlencoded" `
  -Body "grant_type=password&client_id=private&client_secret=YOUR_CLIENT_SECRET&username=admin@example.com&password=YOUR_PASSWORD"

$token = $tokenResponse.access_token
```

### Test Invalid User Creation (Missing Name):
```powershell
try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/users" `
      -Method POST `
      -ContentType "application/json" `
      -Headers @{ Authorization = "Bearer $token" } `
      -Body "{}"
} catch {
    Write-Host "Expected error: $($_.Exception.Response.StatusCode)"
    Write-Host "Response: $($_.Exception.Response | ConvertTo-Json)"
}
```

### Test Valid User Creation:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/users" `
  -Method POST `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $token" } `
  -Body '{"name": "Test User"}'
```

## Expected Results

- **Missing name** (`{}`): Should return **400 Bad Request** with validation error
- **Empty name** (`{"name": ""}`): Should return **400 Bad Request** 
- **Blank name** (`{"name": "   "}`): Should return **400 Bad Request**
- **Valid name** (`{"name": "Test User"}`): Should return **201 Created**

## Verification

The unit tests already prove the validation logic works:
- ✅ `UserServiceTest.save_withMissingName_shouldThrowConstraintViolationException` - PASSED
- ✅ `UserServiceTest.save_withEmptyName_shouldThrowConstraintViolationException` - PASSED  
- ✅ `UserServiceTest.save_withBlankName_shouldThrowConstraintViolationException` - PASSED