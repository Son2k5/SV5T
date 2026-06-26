package com.example.SinhVien5T.common.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        try {
            log.info("Starting database migration: dropping old unique index uk_application_record_user_campaign");
            jdbcTemplate.execute("ALTER TABLE application_record DROP INDEX uk_application_record_user_campaign");
            log.info("Dropped index uk_application_record_user_campaign successfully");
        } catch (Exception e) {
            log.info("Index uk_application_record_user_campaign does not exist or already dropped: " + e.getMessage());
        }

        try {
            log.info("Starting database migration: dropping old unique index uk_application_record_user_campaign_type");
            jdbcTemplate.execute("ALTER TABLE application_record DROP INDEX uk_application_record_user_campaign_type");
            log.info("Dropped index uk_application_record_user_campaign_type successfully");
        } catch (Exception e) {
            log.info("Index uk_application_record_user_campaign_type does not exist or already dropped: " + e.getMessage());
        }

        try {
            log.info("Starting database migration: setting null is_group values to false in application_record");
            int updatedRows = jdbcTemplate.update("UPDATE application_record SET is_group = false WHERE is_group IS NULL");
            log.info("Successfully updated {} rows with null is_group", updatedRows);
        } catch (Exception e) {
            log.error("Failed to update null is_group values: " + e.getMessage());
        }

        try {
            log.info("Starting database migration: changing campaign.is_group to VARCHAR");
            jdbcTemplate.execute("UPDATE campaign SET is_group = '0' WHERE is_group IS NULL");
            jdbcTemplate.execute("ALTER TABLE campaign MODIFY COLUMN is_group VARCHAR(20) NOT NULL DEFAULT 'INDIVIDUAL'");
            jdbcTemplate.execute("UPDATE campaign SET is_group = 'INDIVIDUAL' WHERE is_group = '0' OR is_group = 'false'");
            jdbcTemplate.execute("UPDATE campaign SET is_group = 'GROUP' WHERE is_group = '1' OR is_group = 'true'");
            log.info("Successfully migrated campaign.is_group column");
        } catch (Exception e) {
            log.error("Failed to migrate campaign.is_group column: " + e.getMessage());
        }

        try {
            log.info("Starting database migration: changing campaign.level to VARCHAR(30)");
            jdbcTemplate.execute("UPDATE campaign SET level = 'UNIVERSITY' WHERE level IS NULL");
            jdbcTemplate.execute("ALTER TABLE campaign MODIFY COLUMN level VARCHAR(30) NOT NULL");
            log.info("Successfully migrated campaign.level column");
        } catch (Exception e) {
            log.error("Failed to migrate campaign.level column: " + e.getMessage());
        }

        try {
            log.info("Starting database migration: changing standard.level to VARCHAR(30)");
            jdbcTemplate.execute("ALTER TABLE standard MODIFY COLUMN level VARCHAR(30) NULL");
            log.info("Successfully migrated standard.level column");
        } catch (Exception e) {
            log.error("Failed to migrate standard.level column: " + e.getMessage());
        }

        try {
            log.info("Starting database migration: changing application_record.level to VARCHAR(30)");
            jdbcTemplate.execute("ALTER TABLE application_record MODIFY COLUMN level VARCHAR(30) NULL");
            log.info("Successfully migrated application_record.level column");
        } catch (Exception e) {
            log.error("Failed to migrate application_record.level column: " + e.getMessage());
        }
    }
}
