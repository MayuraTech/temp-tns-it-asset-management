# Quick Start Guide - IT Asset Management

## Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Maven 3.6+
- npm or yarn

## Backend Setup (SQL Server Required)

### 1. Setup SQL Server Database

**IMPORTANT**: If you're switching from H2 or have an existing database with wrong schema, run this first:

```sql
-- Open SQL Server Management Studio or Azure Data Studio
-- Connect to: TNS-IT-DESKTOP\SQLEXPRESS
-- Run this script:

USE master;
GO

ALTER DATABASE IT_Asset SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE IT_Asset;
GO

CREATE DATABASE IT_Asset;
GO
```

Or simply execute the provided script: `FIX_SQL_SERVER_SCHEMA_NOW.sql`

### 2. Navigate to backend directory
```bash
cd backend
```

### 3. Start the backend with development profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The backend will:
- Start on `http://localhost:8080`
- Connect to SQL Server at `TNS-IT-DESKTOP\SQLEXPRESS`
- Use database `IT_Asset`
- Auto-create database schema (if database is empty)
- Seed default users (admin, manager, viewer)

### 3. Verify backend is running
```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Access Swagger UI
# Open browser: http://localhost:8080/swagger-ui.html
```

**Expected logs on successful startup:**
```
Hibernate: create table Users (...)
DataInitializer: Created user: admin
DataInitializer: Created user: manager
DataInitializer: Created user: viewer
Started ItAssetManagementApplication in X.XXX seconds
```

## Frontend Setup

### 1. Navigate to frontend directory
```bash
cd frontend
```

### 2. Install dependencies (first time only)
```bash
npm install
```

### 3. Start the development server
```bash
npm start
```

The frontend will:
- Start on `http://localhost:4200`
- Auto-reload on code changes
- Connect to backend at `http://localhost:8080`

### 4. Access the application
Open your browser and navigate to: `http://localhost:4200`

## Default Login Credentials

The application automatically seeds default users on first startup (dev profile only):

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| `admin` | `Admin@123456` | Administrator | Full system access |
| `manager` | `Manager@123456` | Asset Manager | Can manage assets |
| `viewer` | `Viewer@123456` | Viewer | Read-only access |

**Note**: These users are only created when the database is empty. If you need to reset, drop and recreate the database using `FIX_SQL_SERVER_SCHEMA_NOW.sql`.

## Common Commands

### Backend

```bash
# Run with dev profile (SQL Server database)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvn test

# Build JAR
mvn clean package

# Clean Maven cache (after dependency changes)
mvn clean
```

### Frontend

```bash
# Install dependencies
npm install

# Start dev server
npm start

# Build for production
npm run build:prod

# Run tests
npm test

# Run linter
npm run lint
```

## Troubleshooting

### Backend won't start - "Invalid column name" error

**Problem**: `Invalid column name 'account_locked'` or similar errors

**Root Cause**: Database has old schema from H2 with snake_case column names

**Solution**: Drop and recreate the database:
1. Run `FIX_SQL_SERVER_SCHEMA_NOW.sql` in SQL Server Management Studio
2. Restart backend: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
3. See `CURRENT_STATUS_AND_FIX.md` for detailed instructions

### Backend won't start - Connection error

**Problem**: Cannot connect to SQL Server

**Solution**: 
1. Verify SQL Server is running
2. Check connection details in `backend/src/main/resources/application-dev.properties`:
   - Server: `TNS-IT-DESKTOP\SQLEXPRESS`
   - Database: `IT_Asset`
   - Username: `itassetuser`
   - Password: `its@2345`
3. Verify SQL Server authentication mode allows SQL Server authentication

### Frontend build errors

**Problem**: TypeScript compilation errors

**Solution**: The recent fixes should have resolved these. If issues persist:
```bash
cd frontend
npm install
npm start
```

### Port already in use

**Backend (8080)**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

**Frontend (4200)**:
```bash
# Windows
netstat -ano | findstr :4200
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:4200 | xargs kill -9
```

### H2 Console Access

**Not applicable** - This project uses SQL Server for development.

To view database contents, use:
- SQL Server Management Studio (SSMS)
- Azure Data Studio
- Any SQL Server client tool

Connection details:
- Server: `TNS-IT-DESKTOP\SQLEXPRESS`
- Database: `IT_Asset`
- Username: `itassetuser`
- Password: `its@2345`

## Development Workflow

### 1. Start Backend
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. Start Frontend (in new terminal)
```bash
cd frontend
npm start
```

### 3. Make Changes
- Backend: Edit Java files, Maven will auto-reload
- Frontend: Edit TypeScript/HTML/SCSS files, browser will auto-reload

### 4. Test Your Changes
- Backend: `mvn test`
- Frontend: `npm test`

## API Documentation

Once the backend is running, access the interactive API documentation:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

## Project Structure

```
.
├── backend/                    # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Java source code
│   │   │   └── resources/     # Configuration files
│   │   └── test/              # Test files
│   └── pom.xml                # Maven configuration
│
├── frontend/                   # Angular frontend
│   ├── src/
│   │   ├── app/               # Application code
│   │   ├── assets/            # Static assets
│   │   └── styles.scss        # Global styles
│   ├── angular.json           # Angular configuration
│   └── package.json           # npm dependencies
│
└── README.md                   # Project documentation
```

## Next Steps

1. ✅ Start backend with dev profile
2. ✅ Start frontend
3. ✅ Login with default credentials
4. 🎯 Explore the application
5. 🎯 Check out the API documentation
6. 🎯 Start developing!

## Need Help?

- Check `CURRENT_STATUS_AND_FIX.md` for SQL Server schema fix
- Check `BACKEND_RESTART_INSTRUCTIONS.md` for restart procedures
- Check `SQL_SERVER_SCHEMA_FIX.md` for database schema issues
- Check `SCHEMA_COMPARISON.md` for schema details
- Check `FRONTEND_BUILD_FIXES.md` for frontend build issue resolutions
- Review the steering documents in `.kiro/steering/` for coding standards
