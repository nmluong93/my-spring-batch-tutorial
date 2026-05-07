CREATE DATABASE IF NOT EXISTS `my-spring-batch`;
USE `my-spring-batch`;

CREATE TABLE IF NOT EXISTS employees (
  employee_id     VARCHAR(20)  NOT NULL,
  first_name      VARCHAR(100) NOT NULL,
  last_name       VARCHAR(100) NOT NULL,
  email           VARCHAR(255) NOT NULL,
  phone           VARCHAR(30)  NOT NULL,
  department      VARCHAR(100) NOT NULL,
  job_title       VARCHAR(150) NOT NULL,
  employment_type VARCHAR(20)  NOT NULL,
  hire_date       DATE         NOT NULL,
  salary          INT          NOT NULL,
  manager_id      VARCHAR(20)      NULL,
  office_location VARCHAR(150) NOT NULL,
  status          VARCHAR(20)  NOT NULL,
  gender          VARCHAR(10)  NOT NULL,
  birth_date      DATE         NOT NULL,
  PRIMARY KEY (employee_id),
  UNIQUE KEY uq_email (email)
);
