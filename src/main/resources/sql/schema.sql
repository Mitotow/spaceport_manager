CREATE TABLE IF NOT EXISTS app_user (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    login         VARCHAR(50) NOT NULL UNIQUE,
    password_salt CHAR(32)    NOT NULL,
    password_hash CHAR(64)    NOT NULL
);

CREATE TABLE IF NOT EXISTS flight (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    code           VARCHAR(20)  NOT NULL UNIQUE,
    ship           VARCHAR(100) NOT NULL,
    origin         VARCHAR(100) NOT NULL,
    destination    VARCHAR(100) NOT NULL,
    departure_time DATETIME     NOT NULL,
    arrival_time   DATETIME     NOT NULL,
    dock           VARCHAR(20),
    capacity       INT          NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    CONSTRAINT chk_flight_capacity CHECK (capacity > 0),
    CONSTRAINT chk_flight_times CHECK (arrival_time > departure_time)
);

CREATE TABLE IF NOT EXISTS passenger (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    last_name       VARCHAR(100) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    birth_date      DATE         NOT NULL,
    origin_planet   VARCHAR(100) NOT NULL,
    passport_number VARCHAR(30)  NOT NULL UNIQUE,
    email           VARCHAR(150),
    flight_id       INT,
    CONSTRAINT fk_passenger_flight FOREIGN KEY (flight_id) REFERENCES flight (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS baggage (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    tag_number   VARCHAR(20)  NOT NULL UNIQUE,
    type         VARCHAR(20)  NOT NULL,
    weight_kg    DECIMAL(6,2) NOT NULL,
    description  VARCHAR(255),
    fragile      BOOLEAN      NOT NULL DEFAULT FALSE,
    passenger_id INT          NOT NULL,
    CONSTRAINT fk_baggage_passenger FOREIGN KEY (passenger_id) REFERENCES passenger (id) ON DELETE CASCADE,
    CONSTRAINT chk_baggage_weight CHECK (weight_kg > 0)
);
