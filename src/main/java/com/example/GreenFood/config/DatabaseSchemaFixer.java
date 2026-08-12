package com.example.GreenFood.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaFixer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaFixer.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        relaxLegacyProductCategoryColumn();
    }

    /**
     * Legacy schema keeps a camelCase {@code categoryId} column alongside {@code category_id}.
     * Hibernate writes only {@code category_id}, so inserts fail until the legacy column allows NULL.
     */
    private void relaxLegacyProductCategoryColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE product MODIFY COLUMN categoryId INT NULL DEFAULT NULL");
            log.info("Relaxed legacy product.categoryId column to allow NULL");
        } catch (Exception ex) {
            log.debug("Skipped product.categoryId schema fix: {}", ex.getMessage());
        }
    }
}
