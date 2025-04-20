USE skillconnect;

-- Rename the applications table to project_applications
RENAME TABLE applications TO project_applications;

-- Update any views or triggers that might reference the old table name
-- (Add these if needed)