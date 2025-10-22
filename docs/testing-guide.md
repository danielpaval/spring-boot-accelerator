# Testing Guide

## Manual Testing for User Validation

### Prerequisites
1. Start the application: `.\gradlew bootRun`
2. Ensure Keycloak is running at `http://localhost:8180/realms/dev`
3. Have admin credentials configured

### Test Case: Admin user creating user without name should return 400 Bad Request

#### Step 1: Get Admin Token
```http
POST http://localhost:8180/realms/dev/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=private&client_secret={{CLIENT_SECRET}}&username=admin@example.com&password={{USER_PASSWORD}}
```

#### Step 2: Test Invalid User Creation (Missing Name)
```http
POST http://localhost:8080/api/users
Content-Type: application/json
Authorization: Bearer {{JWT_TOKEN_FROM_STEP_1}}

{}
```

Expected: **400 Bad Request** with validation error message

#### Step 3: Test Invalid User Creation (Empty Name)
```http
POST http://localhost:8080/api/users
Content-Type: application/json
Authorization: Bearer {{JWT_TOKEN_FROM_STEP_1}}

{
  "name": ""
}
```

Expected: **400 Bad Request** with validation error message

#### Step 4: Test Invalid User Creation (Blank Name)
```http
POST http://localhost:8080/api/users
Content-Type: application/json
Authorization: Bearer {{JWT_TOKEN_FROM_STEP_1}}

{
  "name": "   "
}
```

Expected: **400 Bad Request** with validation error message

#### Step 5: Test Valid User Creation
```http
POST http://localhost:8080/api/users
Content-Type: application/json
Authorization: Bearer {{JWT_TOKEN_FROM_STEP_1}}

{
  "name": "Test User"
}
```

Expected: **201 Created** with user data

### Using the Existing api.rest File

You can use the existing `api.rest` file and modify the user creation section:

1. Update `http-client.env.json` with your environment variables
2. Execute the "Get ADMIN access token" request
3. Modify the "Create user" request to test different scenarios

### PowerShell/cURL Alternative

```powershell
# Get admin token
$tokenResponse = Invoke-RestMethod -Uri "http://localhost:8180/realms/dev/protocol/openid-connect/token" `
  -Method POST `
  -ContentType "application/x-www-form-urlencoded" `
  -Body "grant_type=password&client_id=private&client_secret=$CLIENT_SECRET&username=admin@example.com&password=$USER_PASSWORD"

$token = $tokenResponse.access_token

# Test invalid user creation (missing name)
Invoke-RestMethod -Uri "http://localhost:8080/api/users" `
  -Method POST `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $token" } `
  -Body "{}"
```

## Unit Testing Approach

For unit tests that don't require full integration, see the UserServiceTest.java file that tests the service layer in isolation.