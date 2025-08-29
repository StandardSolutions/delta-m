package com.stdsolutions.deltam.core;

import com.stdsolutions.deltam.DeltaM;
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
    private DeltaM deltaM;

    @BeforeEach
    void setUp() {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");
        
        dataSource = h2DataSource;
        // Используем H2-совместимые миграции
        deltaM = new DeltaM(dataSource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Очищаем созданные таблицы после каждого теста
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
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
        assertDoesNotThrow(() -> deltaM.init());

        // Проверяем, что таблицы были созданы
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Проверяем таблицу changelog - H2 создает таблицы в верхнем регистре
            ResultSet resultSet = statement.executeQuery(
                "SELECT table_name FROM information_schema.tables " +
                "WHERE table_name = 'DELTA_M_CHANGELOG'"
            );
            assertTrue(resultSet.next(), "Changelog table should be created");
        }
    }

    @Test
    void testExecuteMultipleTimes() throws SQLException {
        // Первый запуск должен пройти успешно
        assertDoesNotThrow(() -> deltaM.init());
        
        // Второй запуск также должен пройти успешно (idempotent)
        assertDoesNotThrow(() -> deltaM.init());
        
        // Проверяем, что таблицы все еще существуют
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            ResultSet resultSet = statement.executeQuery(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_name = 'DELTA_M_CHANGELOG'"
            );
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt(1), "Changelog table should exist");
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
        assertNotNull(deltaM, "DeltaM should not be null");
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
