DROP PROCEDURE IF EXISTS DropIndexIfExists;
DELIMITER //
CREATE PROCEDURE DropIndexIfExists()
BEGIN
    DECLARE index_count INT;
    
    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics 
    WHERE table_schema = DATABASE() 
      AND table_name = 'application_record' 
      AND index_name = 'uk_application_record_user_campaign';
      
    IF index_count > 0 THEN
        ALTER TABLE application_record DROP INDEX uk_application_record_user_campaign;
    END IF;
    
    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics 
    WHERE table_schema = DATABASE() 
      AND table_name = 'application_record' 
      AND index_name = 'uk_application_record_user_campaign_type';
      
    IF index_count > 0 THEN
        ALTER TABLE application_record DROP INDEX uk_application_record_user_campaign_type;
    END IF;
END //
DELIMITER ;
CALL DropIndexIfExists();
DROP PROCEDURE DropIndexIfExists;
