--
-- (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright
-- law. All use of this software is subject to MuleSoft's Master Subscription Agreement
-- (or other master license agreement) separately entered into in writing between you and
-- MuleSoft. If such an agreement is not in place, you may not use the software.
--

CREATE TABLE MULE_API_ANALYTICS (
  id                           CHAR(36)     NOT NULL,
  api_id                       INT          NOT NULL,
  api_name                     VARCHAR(64)  NULL,
  api_version                  VARCHAR(150) NULL,
  api_version_id               INT          NOT NULL,
  application_name             VARCHAR(42)  NULL,
  client_id                    VARCHAR(255) NULL,
  client_ip                    VARCHAR(45)  NOT NULL,
  event_id                     VARCHAR(36)  NOT NULL,
  host_id                      VARCHAR(255) NULL,
  org_id                       VARCHAR(36)  NULL,
  path                         VARCHAR(500) NOT NULL,
  policy_violation_policy_id   VARCHAR(50)  NULL,
  policy_violation_policy_name VARCHAR(150) NULL,
  policy_violation_outcome     VARCHAR(10)  NULL,
  received_ts                  VARCHAR(30)  NOT NULL,
  replied_ts                   VARCHAR(30)  NOT NULL,
  request_bytes                INT          NOT NULL,
  request_disposition          VARCHAR(10)  NOT NULL,
  response_bytes               INT          NOT NULL,
  status_code                  INT          NOT NULL,
  transaction_id               VARCHAR(36)  NULL,
  user_agent                   VARCHAR(500) NULL,
  verb                         VARCHAR(8)   NOT NULL,
  PRIMARY KEY (id)
);
