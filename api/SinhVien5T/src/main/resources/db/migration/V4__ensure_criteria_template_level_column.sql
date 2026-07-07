SET @criteria_template_level_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'criteria_template'
      AND column_name = 'level'
);

SET @criteria_template_level_sql = IF(
    @criteria_template_level_exists = 0,
    'ALTER TABLE criteria_template ADD COLUMN level VARCHAR(30) NULL',
    'SELECT 1'
);

PREPARE criteria_template_level_stmt FROM @criteria_template_level_sql;
EXECUTE criteria_template_level_stmt;
DEALLOCATE PREPARE criteria_template_level_stmt;
