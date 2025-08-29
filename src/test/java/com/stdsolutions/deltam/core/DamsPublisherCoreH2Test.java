package com.stdsolutions.deltam.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DamsPublisherCoreH2Test {

    private DataSource dataSource;
    private DamsPublisherCore damsPublisherCore;

    @BeforeEach
    void setUp() {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");
        
        dataSource = h2DataSource;
        // Используем H2-совместимые миграции
        damsPublisherCore = new DamsPublisherCore(dataSource, "--migration-path=h2-migrations");
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Очищаем созданные таблицы после каждого теста
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS dams_changelog_table CASCADE");
            statement.execute("DROP TABLE IF EXISTS schema_initialization_lock CASCADE");
        }
    }

    @Test
    void testExecuteWithH2() throws SQLException {
        // Проверяем, что база данных действительно H2
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            assertTrue(productName.equalsIgnoreCase("H2"), 
                "Database should be H2, but was: " + productName);
        }

        // Выполняем основной метод
        assertDoesNotThrow(() -> damsPublisherCore.execute());

        // Проверяем, что таблицы были созданы
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Проверяем таблицу dams_changelog_table
            ResultSet resultSet = statement.executeQuery(
                "SELECT table_name FROM information_schema.tables " +
                "WHERE table_name = 'DAMS_CHANGELOG_TABLE'"
            );
            assertTrue(resultSet.next(), "Table 'DAMS_CHANGELOG_TABLE' should be created");
            
            // Проверяем таблицу schema_initialization_lock
            resultSet = statement.executeQuery(
                "SELECT table_name FROM information_schema.tables " +
                "WHERE table_name = 'SCHEMA_INITIALIZATION_LOCK'"
            );
            assertTrue(resultSet.next(), "Table 'SCHEMA_INITIALIZATION_LOCK' should be created");
        }
    }

    @Test
    void testExecuteMultipleTimes() throws SQLException {
        // Первый запуск должен пройти успешно
        assertDoesNotThrow(() -> damsPublisherCore.execute());
        
        // Второй запуск также должен пройти успешно (idempotent)
        assertDoesNotThrow(() -> damsPublisherCore.execute());
        
        // Проверяем, что таблицы все еще существуют
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            ResultSet resultSet = statement.executeQuery(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_name IN ('DAMS_CHANGELOG_TABLE', 'SCHEMA_INITIALIZATION_LOCK')"
            );
            assertTrue(resultSet.next());
            assertEquals(2, resultSet.getInt(1), "Both tables should still exist");
        }
    }

    @Test
    void testDatabaseConnection() {
        assertDoesNotThrow(() -> {
            try (Connection connection = dataSource.getConnection()) {
                assertFalse(connection.isClosed());
                assertTrue(connection.isValid(5));
            }
        });
    }

    @Test
    void testDataSourceNotNull() {
        assertNotNull(dataSource, "DataSource should not be null");
        assertNotNull(damsPublisherCore, "DamsPublisherCore should not be null");
    }

    @Test
    void testDatabaseTypeDetection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            String productVersion = metaData.getDatabaseProductVersion();
            
            assertNotNull(productName, "Database product name should not be null");
            assertNotNull(productVersion, "Database product version should not be null");
            assertEquals("H2", productName, "Database should be H2");
        }
    }
}
