ALTER TABLE MULE_API_ANALYTICS ADD temp VARCHAR(50);
update MULE_API_ANALYTICS set temp=cast(policy_violation_policy_id as VARCHAR(50));
ALTER TABLE MULE_API_ANALYTICS DROP COLUMN policy_violation_policy_id;
ALTER TABLE MULE_API_ANALYTICS RENAME COLUMN temp TO policy_violation_policy_id;