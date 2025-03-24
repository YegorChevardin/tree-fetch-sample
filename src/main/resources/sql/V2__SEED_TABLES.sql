INSERT INTO projects (id, name, parent_id, parent_name, root_id, root_name) VALUES
('5a16875d-7229-436a-8726-1582edfa5f29', 'Project 1', NULL, NULL, null, null),
('11f296fb-2c6f-4cb0-93dc-bbe6cc8898e6', 'Project 2', '5a16875d-7229-436a-8726-1582edfa5f29', 'Project 1', '5a16875d-7229-436a-8726-1582edfa5f29', 'Project 1'),
('6280e78c-5eca-4ed3-a4ed-484b01847573', 'Project 3', '11f296fb-2c6f-4cb0-93dc-bbe6cc8898e6', 'Project 2', '5a16875d-7229-436a-8726-1582edfa5f29', 'Project 1'),
('fabaeb36-6e76-4763-b59c-ca5aea7d740d', 'Project 4', '11f296fb-2c6f-4cb0-93dc-bbe6cc8898e6', 'Project 2', '5a16875d-7229-436a-8726-1582edfa5f29', 'Project 1'),
('67cf28ee-1d1d-419e-bbb5-0e4d7907d069', 'Project 5', '5a16875d-7229-436a-8726-1582edfa5f29', 'Project 1', '5a16875d-7229-436a-8726-1582edfa5f29', 'Project 1'),
('777d482a-ebdc-43fc-a55c-11beb7ff46eb', 'Project 6', null, null, null, null),
('85e36c04-517a-436d-a250-acfa56548407', 'Project 7', '777d482a-ebdc-43fc-a55c-11beb7ff46eb', 'Project 6', '777d482a-ebdc-43fc-a55c-11beb7ff46eb', 'Project 6'),
('c5fee1e9-3e9f-4081-9b74-28099a3bb3f6', 'Project 8', null, null, null, null);

INSERT INTO users (id, name, project_id) VALUES
                                             ('5a16875d-7229-436a-8726-1582edfa5f29', 'User 1', '5a16875d-7229-436a-8726-1582edfa5f29'),
                                             ('6280e78c-5eca-4ed3-a4ed-484b01847573', 'User 3', '6280e78c-5eca-4ed3-a4ed-484b01847573'),
                                             ('fabaeb36-6e76-4763-b59c-ca5aea7d740d', 'User 4', 'fabaeb36-6e76-4763-b59c-ca5aea7d740d'),
                                             ('777d482a-ebdc-43fc-a55c-11beb7ff46eb', 'User 6', '85e36c04-517a-436d-a250-acfa56548407');