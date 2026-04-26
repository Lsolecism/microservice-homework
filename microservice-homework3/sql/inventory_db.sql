CREATE DATABASE IF NOT EXISTS inventory_db DEFAULT CHARSET utf8mb4;
USE inventory_db;

DROP TABLE IF EXISTS inventory;
CREATE TABLE inventory (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL UNIQUE,
  total INT NOT NULL,
  used INT NOT NULL,
  residue INT NOT NULL
);

INSERT INTO inventory(product_id, total, used, residue)
VALUES (1, 100, 0, 100);

DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  branch_id BIGINT NOT NULL,
  xid VARCHAR(128) NOT NULL,
  context VARCHAR(128) NOT NULL,
  rollback_info LONGBLOB NOT NULL,
  log_status INT NOT NULL,
  log_created DATETIME NOT NULL,
  log_modified DATETIME NOT NULL,
  ext VARCHAR(100),
  PRIMARY KEY (id),
  UNIQUE KEY ux_undo_log (xid, branch_id)
);
