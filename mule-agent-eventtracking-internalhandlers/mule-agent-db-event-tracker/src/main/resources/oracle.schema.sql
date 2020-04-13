--
-- (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
-- law. All use of this software is subject to MuleSoft's Master Subscription Agreement
-- (or other master license agreement) separately entered into in writing between you and
-- MuleSoft. If such an agreement is not in place, you may not use the software.
--

CREATE TABLE MULE_EVENTS (
  id                  CHAR(36)     NOT NULL,
  action              VARCHAR(500) NULL,
  application         VARCHAR(500) NULL,
  mule_message        CLOB         NULL,
  mule_message_id     VARCHAR(36)  NULL,
  notification_type   VARCHAR(500) NULL,
  path                VARCHAR(500) NULL,
  resource_identifier VARCHAR(500) NULL,
  timestamp           NUMBER       NOT NULL,
  source              CLOB         NULL,
  PRIMARY KEY (id)
);

CREATE TABLE MULE_EVENTS_ANNOTATIONS (
  id               CHAR(36)     NOT NULL,
  event_id         CHAR(36)     NOT NULL,
  annotation_type  VARCHAR(100) NULL,
  annotation_value VARCHAR(255) NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_MEA_ME
  FOREIGN KEY (event_id) REFERENCES MULE_EVENTS (id) ON DELETE CASCADE
);

CREATE INDEX FK_MAE_IDX ON MULE_EVENTS_ANNOTATIONS (event_id);

CREATE TABLE MULE_EVENTS_BUSINESS (
  id             CHAR(36)     NOT NULL,
  event_id       CHAR(36)     NOT NULL,
  business_key   VARCHAR(30)  NOT NULL,
  business_value VARCHAR(255) NULL,
  PRIMARY KEY (id),
  CONSTRAINT FK_MEB_ME
  FOREIGN KEY (event_id) REFERENCES MULE_EVENTS (id) ON DELETE CASCADE
);

CREATE INDEX FK_MEB_IDX ON MULE_EVENTS_BUSINESS (event_id);
