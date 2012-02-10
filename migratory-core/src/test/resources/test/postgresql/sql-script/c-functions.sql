--
-- Copyright (C) 2012 Ness Computing, Inc.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

/* $PostgreSQL: pgsql/contrib/uuid-ossp/uuid-ossp.sql.in,v 1.6 2007/11/13 04:24:29 momjian Exp $ */

-- Adjust this setting to control where the objects get created.
SET search_path = public;

CREATE OR REPLACE FUNCTION uuid_nil()
RETURNS uuid
AS '$libdir/uuid-ossp', 'uuid_nil'
IMMUTABLE STRICT LANGUAGE C;

CREATE OR REPLACE FUNCTION uuid_ns_dns()
RETURNS uuid
AS '$libdir/uuid-ossp', 'uuid_ns_dns'
IMMUTABLE STRICT LANGUAGE C;
