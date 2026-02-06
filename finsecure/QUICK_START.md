# FinSecure - Quick Start Guide

This guide will help you get the FinSecure Banking System up and running in minutes.

## Prerequisites Checklist

- [ ] Java 17 installed (`java -version`)
- [ ] Maven 3.6+ installed (`mvn -version`)
- [ ] Node.js 16+ installed (`node -version`)
- [ ] PostgreSQL or MySQL installed
- [ ] Git (optional, for version control)

## Step-by-Step Setup

### 1. Database Setup (5 minutes)

**MySQL Setup (Recommended)**

```bash
# Login to MySQL
mysql -u root -p

# Run the complete setup script
mysql -u root -p < backend/database-schema-mysql.sql

# Or manually:
# In MySQL prompt:
CREATE DATABASE finsecure_db DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
CREATE USER 'finsecure_user'@'localhost' IDENTIFIED BY 'finsecure_pass';
GRANT ALL PRIVILEGES ON finsecure_db.* TO 'finsecure_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# Import schema
mysql -u finsecure_user -p finsecure_db < backend/database-schema-mysql.sql
```

**Alternative: PostgreSQL**

```bash
# Create database
createdb finsecure_db

# Create user
psql -c "CREATE USER finsecure_user WITH PASSWORD 'finsecure_pass';"
psql -c "GRANT ALL PRIVILEGES ON DATABASE finsecure_db TO finsecure_user;"

# Import schema
psql -U finsecure_user -d finsecure_db -f backend/database-schema.sql

# Update application.properties to use PostgreSQL
```

### 2. Backend Configuration (3 minutes)

**Edit application.properties:**

```bash
cd backend/src/main/resources
nano application.properties
```

**Essential Configuration:**

1. **Database** (choose one):
   ```properties
   # PostgreSQL
   spring.datasource.url=jdbc:postgresql://localhost:5432/finsecure_db
   
   # OR MySQL
   spring.datasource.url=jdbc:mysql://localhost:3306/finsecure_db
   ```

2. **Email** (for OTP):
   ```properties
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   ```
   
   📧 **Gmail Setup:**
   - Enable 2FA on your Google account
   - Generate App Password: https://myaccount.google.com/apppasswords
   - Use the generated 16-character password

3. **JWT Secret** (IMPORTANT - Change this!):
   ```properties
   jwt.secret=YOUR_VERY_SECURE_RANDOM_256_BIT_SECRET_KEY_HERE
   ```
   
   Generate a secure key:
   ```bash
   openssl rand -base64 32
   ```

**Generate SSL Certificate:**

```bash
cd backend/src/main/resources
keytool -genkeypair -alias finsecure -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -validity 3650 \
  -dname "CN=localhost, OU=IT, O=FinSecure, L=City, ST=State, C=US"
```

When prompted, use password: `finsecure123`

### 3. Start Backend (2 minutes)

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

✅ Backend should start at: `https://localhost:8080/api`

You should see:
```
FinSecure Banking System Started
Customer Portal: https://localhost:3000
Employee Portal: https://localhost:3001
API Endpoint: https://localhost:8080/api
```

### 4. Start Customer Portal (2 minutes)

**Open a new terminal:**

```bash
cd frontend/customer-portal
npm install
npm start
```

✅ Customer Portal: `https://localhost:3000`

Browser will open automatically. Accept the SSL certificate warning (localhost development).

### 5. Start Employee Portal (2 minutes)

**Open another terminal:**

```bash
cd frontend/employee-portal
npm install
npm start
```

✅ Employee Portal: `https://localhost:3001`

## First Time Usage

### Create Your First Customer Account

1. Open Customer Portal: https://localhost:3000
2. Click "Register"
3. Fill in details:
   - Email: `customer@test.com`
   - Password: `Test@123`
   - First Name: `John`
   - Last Name: `Doe`
   - Date of Birth: `1995-01-01`
   - Phone: `+919876543210`
   - Account Type: `ADULT`
   - Fill in address details
4. Click Register
5. Check your email for OTP
6. Enter OTP to complete registration

### Login as Employee (Default Admin)

1. Open Employee Portal: https://localhost:3001
2. Login with:
   - Email: `admin@finsecure.com`
   - Password: `Admin@123`
3. ⚠️ **CHANGE THIS PASSWORD IMMEDIATELY**

### Verify Customer KYC

1. In Employee Portal, go to "KYC Verification"
2. You'll see pending KYC documents
3. Review and approve/reject

## Testing the System

### Test Customer Features

**Dashboard:**
- View account summary
- Check KYC status

**Transactions:**
1. Go to Transactions
2. Click "Transfer Money"
3. Request OTP
4. Complete transfer with OTP

**Cards:**
1. View cards
2. Block/Unblock card (requires OTP)

**KYC:**
1. Upload documents:
   - Aadhaar (identity)
   - PAN (tax ID)
   - Address proof
   - Photo
2. Wait for employee verification

### Test Employee Features

**KYC Verification:**
1. View pending KYC documents
2. Click on a document
3. Review customer information
4. Approve/Reject (requires OTP)

**Customer Search:**
1. Search for customers
2. View customer details
3. Check KYC status

## Troubleshooting

### Backend Issues

**"Port 8080 already in use"**
```bash
# Find and kill process
lsof -ti:8080 | xargs kill -9
```

**"Database connection failed"**
- Check if database is running
- Verify credentials in application.properties
- Ensure database exists

**"SSL Certificate error"**
- Regenerate certificate following Step 2
- Ensure password matches in application.properties

### Frontend Issues

**"Cannot connect to backend"**
- Verify backend is running on port 8080
- Check browser console for CORS errors
- Accept SSL certificate in browser

**"Dependencies installation failed"**
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Email/OTP Issues

**"OTP not received"**
- Check spam folder
- Verify email configuration
- Check backend logs for errors
- Ensure app password is correct (not regular password)

**"OTP expired"**
- OTPs expire in 10 minutes
- Request a new OTP

## Default Test Data

After running the database schema, you have:

**Admin Employee:**
- Email: `admin@finsecure.com`
- Password: `Admin@123`
- Role: ADMIN
- ⚠️ **CHANGE IMMEDIATELY IN PRODUCTION**

## Next Steps

1. **Create test accounts** for different account types (Student, Minor)
2. **Test all features** (transactions, cards, loans, KYC)
3. **Review security settings** before production
4. **Customize** branding and features
5. **Read full README.md** for advanced configuration

## Production Deployment

Before going to production:

1. ✅ Change all default passwords
2. ✅ Use strong JWT secret
3. ✅ Get real SSL certificates
4. ✅ Configure production database
5. ✅ Set up proper email service
6. ✅ Enable rate limiting
7. ✅ Review security checklist in README.md

## Support

- 📖 Read full [README.md](README.md)
- 🐛 Check troubleshooting section
- 💬 Review code comments
- 🔍 Check Spring Boot and React documentation

## Development Tips

**Hot Reload:**
- Backend: Maven automatically reloads on code changes
- Frontend: React hot-reloads automatically

**Debugging:**
- Backend logs: Console output
- Frontend: Browser DevTools Console
- Network calls: Browser DevTools Network tab

**Database:**
- Use a SQL client to view/modify data
- Check audit_log table for all actions
- Review notification table for system messages

---

🎉 **Congratulations!** You now have a fully functional banking system running locally.

**Total Setup Time:** ~15-20 minutes

Happy coding! 🚀
