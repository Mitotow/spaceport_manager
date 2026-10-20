INSERT INTO flight (code, ship, origin, destination, departure_time, arrival_time, dock, capacity, status) VALUES
    ('LUN-007', 'Selene Express', 'Terre - Spatioport Antoma', 'Lune - Base Tranquillité',
        TIMESTAMP(CURDATE(), '18:45:00'), TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '10:15:00'), 'Q-03', 60, 'BOARDING'),
    ('SPX-101', 'Odyssée III', 'Terre - Spatioport Antoma', 'Mars - Olympus Port',
        TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '08:30:00'), TIMESTAMP(CURDATE() + INTERVAL 4 DAY, '14:00:00'), 'Q-01', 120, 'SCHEDULED'),
    ('MRS-220', 'Red Horizon', 'Terre - Spatioport Antoma', 'Mars - Valles Hub',
        TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '13:00:00'), TIMESTAMP(CURDATE() + INTERVAL 5 DAY, '18:00:00'), 'Q-02', 150, 'CANCELLED'),
    ('JOV-042', 'Galileo Dream', 'Terre - Spatioport Antoma', 'Europe - Station Cryos',
        TIMESTAMP(CURDATE() + INTERVAL 3 DAY, '22:00:00'), TIMESTAMP(CURDATE() + INTERVAL 13 DAY, '06:00:00'), 'Q-07', 80, 'DELAYED'),
    ('TTN-300', 'Huygens', 'Terre - Spatioport Antoma', 'Titan - Kraken Harbor',
        TIMESTAMP(CURDATE() + INTERVAL 7 DAY, '11:00:00'), TIMESTAMP(CURDATE() + INTERVAL 25 DAY, '09:00:00'), 'Q-12', 40, 'SCHEDULED'),
    ('CRS-015', 'Piazzi', 'Cérès - Station Dawn', 'Terre - Spatioport Antoma',
        TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '09:00:00'), TIMESTAMP(CURDATE(), '07:30:00'), 'Q-05', 50, 'ARRIVED');
