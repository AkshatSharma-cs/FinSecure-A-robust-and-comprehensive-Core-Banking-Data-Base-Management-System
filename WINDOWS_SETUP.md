# Windows Setup Guide

## Step 1: Install Prerequisites

### Java 17
1. Download from https://adoptium.net/temurin/releases/?version=17
2. Run installer, check "Set JAVA_HOME variable"
3. Verify: `java -version`

### Maven
1. Download from https://maven.apache.org/download.cgi
2. Extract to `C:\maven`
3. Add `C:\maven\bin` to System PATH
4. Verify: `mvn -version`

### Node.js
1. Download from https://nodejs.org (LTS version)
2. Run installer
3. Verify: `node -v` and `npm -v`

### MySQL 8.0
1. Download MySQL Installer from https://dev.mysql.com/downloads/installer/
2. Install MySQL Server 8.0
3. Set root password during setup (remember it!)
4. Verify: Open MySQL Workbench or MySQL Command Line Client

## Step 2: Database Setup

Open MySQL Command Line Client (or Workbench) and run:

```sql
-- Run the complete schema file
source C:/path/to/finsecure/backend/database/schema.sql;
```

Or copy and paste the contents of `backend/database/schema.sql` into MySQL Workbench.

## Step 3: Backend Configuration

Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/finsecure_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

## Step 4: Build and Run Backend

Open Command Prompt (not PowerShell for compatibility):

```cmd
cd C:\path\to\finsecure\backend
mvn clean install -DskipTests
mvn spring-boot:run
```

Backend starts at: http://localhost:8080

## Step 5: Run Customer Portal

Open a new Command Prompt:

```cmd
cd C:\path\to\finsecure\customer-portal
npm install
npm start
```

Portal opens at: http://localhost:3000

## Step 6: Run Employee Portal

Open another Command Prompt:

```cmd
cd C:\path\to\finsecure\employee-portal
npm install
npm start
```

Portal opens at: http://localhost:3001

## Common Windows Issues

### Port Already in Use
```cmd
netstat -ano | findstr :8080
taskkill /PID <PID_NUMBER> /F
```

### Maven Build Fails
```cmd
mvn clean install -DskipTests -X
```
Check JAVA_HOME: `echo %JAVA_HOME%`

### NPM Permission Error
Run Command Prompt as Administrator, then:
```cmd
npm cache clean --force
npm install
```

### MySQL Connection Refused
- Ensure MySQL service is running: Services → MySQL80 → Start
- Check firewall: allow port 3306

### React App Won't Start on Port 3001
The `package.json` uses `set PORT=3001` (Windows syntax). If on Linux/Mac, change to:
```json
"start": "PORT=3001 react-scripts start"
```
