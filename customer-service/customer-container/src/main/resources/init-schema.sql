DROP SCHEMA IF EXISTS "customer" CASCADE;
CREATE SCHEMA "customer";

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS customer.customers CASCADE;
CREATE TABLE customer.customers (
	id uuid NOT NULL,
	username character varying COLLATE pg_catalog."default" NOT NULL,
	first_name character varying COLLATE pg_catalog."default" NOT NULL,
	last_name character varying COLLATE pg_catalog."default" NOT NULL,
	
	CONSTRAINT customers_pkey PRIMARY KEY (id)
);

DROP TYPE IF EXISTS "customer".outbox_status;
CREATE TYPE "customer".outbox_status AS ENUM ('STARTED', 'COMPLETED', 'FAILED');

DROP TABLE IF EXISTS customer.customer_outbox CASCADE;
CREATE TABLE customer.customer_outbox (
	id uuid NOT NULL,
	payload jsonb NOT NULL,
	outbox_status outbox_status NOT NULL,
	version integer NOT NULL,
	created_at timestamp with time zone NOT NULL,
	processed_at timestamp with time zone,
	
	CONSTRAINT customer_outbox_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX "customer_outbox_unique_index"
    ON "customer".customer_outbox
     (id, outbox_status);

-- DROP MATERIALIZED VIEW IF EXISTS customer.order_customer_m_view;

-- CREATE MATERIALIZED VIEW customer.order_customer_m_view
-- TABLESPACE pg_default
-- AS
-- 	SELECT id, username, first_name, last_name
-- 	FROM customer.customers
-- WITH DATA;

-- refresh materialized VIEW customer.order_customer_m_view;

-- CREATE OR replace function customer.refresh_order_customer_m_view()
-- returns trigger
-- AS '
-- BEGIN
-- 	refresh materialized VIEW customer.order_customer_m_view;
-- 	return null;
-- END;
-- ' LANGUAGE plpgsql;

-- CREATE trigger refresh_order_customer_m_view
-- after INSERT OR UPDATE OR DELETE OR truncate
-- ON customer.customers FOR each statement
-- EXECUTE PROCEDURE customer.refresh_order_customer_m_view();

