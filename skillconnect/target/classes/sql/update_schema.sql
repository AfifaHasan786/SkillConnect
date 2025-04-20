USE skillconnect;

-- Drop and recreate project_applications table
DROP TABLE IF EXISTS project_applications;
CREATE TABLE project_applications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    volunteer_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (volunteer_id) REFERENCES users(id)
);