package com.studygather.common.config;

import com.studygather.support.MySqlTestcontainersConfiguration;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "spring.flyway.clean-disabled=false")
@ActiveProfiles("testcontainers")
@Import(MySqlTestcontainersConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FlywayCleanIntegrationTest {

    private static final List<String> DOMAIN_TABLES = List.of(
            "users",
            "studies",
            "study_members",
            "study_applications"
    );

    @Autowired
    private Flyway flyway;

    @Autowired
    private DataSource dataSource;

    @Test
    void cleanAndMigrateRecreatesEntireSchema() throws SQLException {
        assertEquals(4, countAppliedMigrations());
        assertAllDomainTablesExist(true);

        flyway.clean();

        assertAllDomainTablesExist(false);

        flyway.migrate();

        assertEquals(4, countAppliedMigrations());
        assertAllDomainTablesExist(true);
    }

    private void assertAllDomainTablesExist(boolean expected) throws SQLException {
        for (String tableName : DOMAIN_TABLES) {
            if (expected) {
                assertTrue(tableExists(tableName), tableName + " 테이블이 존재해야 합니다.");
            } else {
                assertFalse(tableExists(tableName), tableName + " 테이블이 제거되어야 합니다.");
            }
        }
    }

    private boolean tableExists(String tableName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             ResultSet tables = connection.getMetaData().getTables(
                     connection.getCatalog(),
                     null,
                     tableName,
                     new String[]{"TABLE"}
             )) {
            return tables.next();
        }
    }

    private int countAppliedMigrations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
