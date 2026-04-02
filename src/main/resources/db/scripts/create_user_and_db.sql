-- Create db and user, not versioned in flyway
CREATE DATABASE paymentdb;
CREATE USER payment_user WITH PASSWORD 'payment_pass';
GRANT ALL PRIVILEGES ON DATABASE paymentdb TO payment_user;

-- Make your user the owner of the database
ALTER DATABASE paymentdb OWNER TO payment_user;
-- Switch to the database
\c paymentdb
-- Make your user the owner of the public schema
ALTER SCHEMA public OWNER TO payment_user;
GRANT ALL ON SCHEMA public TO payment_user;