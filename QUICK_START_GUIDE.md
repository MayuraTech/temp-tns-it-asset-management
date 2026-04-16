# Quick Start Guide - IT Asset Management

## Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Maven 3.6+
- npm or yarn

## Backend Setup (No SQL Server Required!)

### 1. Navigate to backend directory
```bash
cd backend
```

### 2. Start the backend with development profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The backend will:
- Start on `http://localhost:8080`
- Use H2 in-memory database (no SQL Server needed)
- Auto-create database schema
- Enable H2 console at `http://localhost:8080/h2-console`

### 3. Verify backend is running
```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Access Swagger UI
# Open browser: http://localhost:8080/swagger-ui.html
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

**Note**: These users are only created when the database is empty. If you need to reset, restart the application (H2 in-memory database clears on restart).

## Common Commands

### Backend

```bash
# Run with dev profile (H2 database)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvn test

# Build JAR
mvn clean package

# Run with test profile (requires SQL Server)
mvn spring-boot:run -Dspring-boot.run.profiles=test
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

### Backend won't start - Driver mismatch error

**Problem**: `Driver com.microsoft.sqlserver.jdbc.SQLServerDriver claims to not accept jdbcUrl`

**Solution**: Make sure you're running with the dev profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

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

1. Ensure backend is running with dev profile
2. Navigate to: `http://localhost:8080/h2-console`
3. Connection settings:
   - **JDBC URL**: `jdbc:h2:mem:itassetdb`
   - **Username**: `sa`
   - **Password**: (leave empty)
4. Click "Connect"

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

- Check `BACKEND_DATABASE_CONFIG_FIX.md` for database configuration details
- Check `FRONTEND_BUILD_FIXES.md` for frontend build issue resolutions
- Review the steering documents in `.kiro/steering/` for coding standards
