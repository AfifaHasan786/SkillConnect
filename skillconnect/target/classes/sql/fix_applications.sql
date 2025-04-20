USE skillconnect;

-- Backup existing data
CREATE TEMPORARY TABLE temp_project_applications AS
SELECT id, project_id, volunteer_id, status
FROM project_applications;

-- Drop and recreate the project_applications table
DROP TABLE IF EXISTS project_applications;

CREATE TABLE project_applications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    volunteer_id INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'WITHDRAWN') NOT NULL DEFAULT 'PENDING',
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (volunteer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Restore the data
INSERT INTO project_applications (id, project_id, volunteer_id, status)
SELECT id, project_id, volunteer_id, status
FROM temp_project_applications;

-- Drop the temporary table
DROP TEMPORARY TABLE IF EXISTS temp_project_applications;