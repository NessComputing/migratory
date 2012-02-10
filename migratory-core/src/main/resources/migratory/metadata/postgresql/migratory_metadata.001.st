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

CREATE SEQUENCE <table_name>_seq START 100;

CREATE TABLE <table_name> (
    start_version INT NOT NULL,
    end_version INT NOT NULL,
    personality_name CHARACTER VARYING NOT NULL,
    description CHARACTER VARYING NOT NULL,
    type VARCHAR(10) NOT NULL,
    state VARCHAR(10) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    execution_time BIGINT,
    script_name CHARACTER VARYING NOT NULL,
    location CHARACTER VARYING NOT NULL,
    checksum CHARACTER VARYING NOT NULL,
    metadata_user CHARACTER VARYING NOT NULL DEFAULT current_user,
    metadata_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    metadata_id BIGINT NOT NULL DEFAULT nextval('<table_name>_seq') PRIMARY KEY
);
--
-- Add an index for performance reasons.
--
CREATE INDEX <table_name>_personality_index ON <table_name> (personality_name);
