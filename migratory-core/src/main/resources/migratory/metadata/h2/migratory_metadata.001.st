--
-- Copyright (C) 2010-2011 the original author or authors.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

CREATE SEQUENCE <table_name>_seq START WITH 100 INCREMENT BY 1;

CREATE TABLE <table_name> (
    start_version INT NOT NULL,
    end_version INT NOT NULL,
    personality_name VARCHAR(100) NOT NULL,
    description VARCHAR(100),
    type VARCHAR(10) NOT NULL,
    state VARCHAR(10) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    execution_time BIGINT,
    location VARCHAR(200) NOT NULL,
    script_name VARCHAR(100) NOT NULL,
    checksum VARCHAR(40) NOT NULL,
    metadata_user VARCHAR(30) DEFAULT CURRENT_USER(),
    metadata_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata_id INT DEFAULT NEXT VALUE FOR <table_name>_seq PRIMARY KEY
);

CREATE INDEX <table_name>_personality_index ON <table_name> (personality_name);
