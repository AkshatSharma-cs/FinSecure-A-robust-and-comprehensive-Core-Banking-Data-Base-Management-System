# FinSecure - MySQL Database Setup Guide

This guide provides detailed instructions for setting up the MySQL database for FinSecure.

## Prerequisites

- MySQL 8.0 or higher
- MySQL client (command-line or GUI like MySQL Workbench)
- Root or admin access to MySQL server

## Quick Setup (Recommended)

The easiest way to set up the database is to use the provided MySQL schema file:

```bash
# Login to MySQL and run the complete setup script
mysql -u root -p < backend/database-schema-mysql.sql
```

This single command will:
- Create the database with proper charset (utf8mb4)
- Create the user and grant permissions
- Create all 15 tables with proper structure
- Add indexes, triggers, and views
- Insert default admin user

## Manual Setup (Step by Step)

If you prefer to set up manually or need to troubleshoot:

### Step 1: Create Database and User

```bash
# Login to MySQL
mysql -u root -p

# In MySQL prompt:
CREATE DATABASE finsecure_db 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE USER 'finsecure_user'@'localhost' IDENTIFIED BY 'finsecure_pass';
GRANT ALL PRIVILEGES ON finsecure_db.* TO 'finsecure_user'@'localhost';
FLUSH PRIVILEGES;

# Verify database creation
SHOW DATABASES LIKE 'finsecure_db';

# Exit MySQL
EXIT;
```

### Step 2: Import Schema

```bash
# Import the schema file
mysql -u finsecure_user -p finsecure_db < backend/database-schema-mysql.sql

# Enter password: finsecure_pass
```

### Step 3: Verify Installation

```bash
# Login to database
mysql -u finsecure_user -p finsecure_db

# In MySQL prompt:

# Show all tables (should show 15 tables)
SHOW TABLES;

# Verify admin user was created
SELECT email, role FROM User;

# Verify admin employee was created
SELECT first_name, last_name, employee_code, department FROM Employee;

# Check table structure (example)
DESCRIBE Customer;

# Exit
EXIT;
```

## Database Configuration

### Character Set: utf8mb4

The database uses `utf8mb4` character set which provides:
- Full Unicode support (including emojis)
- Better international character support
- 4-byte UTF-8 encoding

### Collation: utf8mb4_unicode_ci

- Case-insensitive comparisons
- Better sorting for international characters

### Storage Engine: InnoDB

All tables use InnoDB which provides:
- ACID compliance
- Foreign key constraints
- Transaction support
- Row-level locking

## Database Structure

### 15 Tables Created

1. **User** - Authentication and user roles
2. **Customer** - Customer profiles
3. **Employee** - Employee information
4. **Account** - Bank accounts
5. **Transaction** - All transactions
6. **Card** - Card management
7. **Loan** - Loan applications
8. **Loan_EMI** - EMI schedules
9. **Investment** - Investment products
10. **Insurance_Policy** - Insurance policies
11. **KYC_Document** - KYC documents
12. **OTP** - OTP verification
13. **Notification** - User notifications
14. **Audit_Log** - Audit trail
15. **Ads** - Advertisements

### Indexes

Performance indexes created on:
- Primary keys (automatic)
- Foreign keys
- Email, phone (unique)
- Status fields
- Date fields for transactions
- Composite indexes for common queries

### Triggers

Two automated triggers:
1. **trg_kyc_approval_account_activation** - Activates accounts when KYC approved
2. **trg_kyc_rejection_card_block** - Blocks cards when KYC rejected

### Views

**vw_customer_dashboard** - Aggregated customer statistics

## Default Credentials

After setup, you'll have one user:

**Admin Employee:**
```
Email: admin@finsecure.com
Password: Admin@123
Role: ADMIN
Employee Code: EMP001
```

⚠️ **IMPORTANT:** Change this password immediately after first login!

## Configuration in Spring Boot

Update `backend/src/main/resources/application.properties`:

```properties
# MySQL Database Configuration (Default)
spring.datasource.url=jdbc:mysql://localhost:3306/finsecure_db?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=finsecure_user
spring.datasource.password=finsecure_pass
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate Configuration
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

## Troubleshooting

### Error: "Database exists"

If database already exists:
```sql
DROP DATABASE IF EXISTS finsecure_db;
-- Then run setup again
```

### Error: "User exists"

```sql
DROP USER IF EXISTS 'finsecure_user'@'localhost';
-- Then create user again
```

### Error: "Access denied"

Make sure you're using correct credentials:
- Root password for initial setup
- finsecure_pass for application user

### Error: "Cannot connect to MySQL"

Check if MySQL is running:
```bash
# Linux/Mac
sudo systemctl status mysql

# Or
ps aux | grep mysql
```

Start MySQL if needed:
```bash
# Linux
sudo systemctl start mysql

# Mac
brew services start mysql
```

### Verify Connection

Test connection:
```bash
mysql -u finsecure_user -p -h localhost finsecure_db
```

## Security Best Practices

### For Development:
- Current credentials are fine
- Ensure MySQL only listens on localhost

### For Production:
1. **Change all passwords**
   ```sql
   ALTER USER 'finsecure_user'@'localhost' IDENTIFIED BY 'strong_random_password';
   ```

2. **Restrict user host**
   ```sql
   CREATE USER 'finsecure_user'@'app_server_ip' IDENTIFIED BY 'password';
   ```

3. **Use SSL/TLS**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/finsecure_db?useSSL=true&requireSSL=true
   ```

4. **Enable binary logging** for point-in-time recovery

5. **Set up regular backups**
   ```bash
   mysqldump -u root -p finsecure_db > backup_$(date +%Y%m%d).sql
   ```

## Performance Tuning

For production, consider:

1. **InnoDB buffer pool**
   ```ini
   # In my.cnf
   innodb_buffer_pool_size = 2G  # 70-80% of available RAM
   ```

2. **Query cache** (if using MySQL < 8.0)

3. **Connection pooling** in application.properties:
   ```properties
   spring.datasource.hikari.maximum-pool-size=10
   spring.datasource.hikari.minimum-idle=5
   ```

## Backup and Restore

### Backup

```bash
# Full backup
mysqldump -u finsecure_user -p finsecure_db > finsecure_backup.sql

# With timestamp
mysqldump -u finsecure_user -p finsecure_db > finsecure_$(date +%Y%m%d_%H%M%S).sql

# Compressed backup
mysqldump -u finsecure_user -p finsecure_db | gzip > finsecure_backup.sql.gz
```

### Restore

```bash
# From SQL file
mysql -u finsecure_user -p finsecure_db < finsecure_backup.sql

# From compressed file
gunzip < finsecure_backup.sql.gz | mysql -u finsecure_user -p finsecure_db
```

## Monitoring

### Check Database Size

```sql
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'finsecure_db'
GROUP BY table_schema;
```

### Check Table Sizes

```sql
SELECT 
    table_name AS 'Table',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)',
    table_rows AS 'Rows'
FROM information_schema.tables
WHERE table_schema = 'finsecure_db'
ORDER BY (data_length + index_length) DESC;
```

### Check Active Connections

```sql
SHOW PROCESSLIST;
```

## Next Steps

After database setup:
1. Configure Spring Boot (application.properties)
2. Generate SSL certificate
3. Start the backend application
4. Verify connection in application logs
5. Test with default admin login

---

**MySQL Version:** 8.0+  
**Character Set:** utf8mb4  
**Collation:** utf8mb4_unicode_ci  
**Storage Engine:** InnoDB
