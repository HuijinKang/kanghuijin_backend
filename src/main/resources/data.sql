INSERT INTO policy_configs (config_key, withdraw_daily_limit, transfer_daily_limit, transfer_fee_bps, created_at, updated_at)
VALUES ('DEFAULT', 1000000, 3000000, 100, UTC_TIMESTAMP(), UTC_TIMESTAMP())
ON DUPLICATE KEY UPDATE
  updated_at = UTC_TIMESTAMP();
