package com.stdsolutions.deltam;

import com.stdsolutions.deltam.DeltaM;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class DeltaMPostgreSQLTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("postgres")
            .withUsername("test")
            .withPassword("test");

    private DataSource dataSource;
    private DeltaM deltaM;

    @BeforeEach
    void setUp() throws SQLException {
        // Создаем DataSource для TestContainers PostgreSQL базы данных
        PGSimpleDataSource pgDataSource = new PGSimpleDataSource();
        pgDataSource.setUrl(postgres.getJdbcUrl());
        pgDataSource.setUser(postgres.getUsername());
        pgDataSource.setPassword(postgres.getPassword());
        
        // Создаем схему delta_m, если она не существует
        try (Connection connection = pgDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS delta_m");
        }
        
        pgDataSource.setCurrentSchema("delta_m");
        dataSource = pgDataSource;
        // Настраиваем имена таблиц для PostgreSQL
        deltaM = new DeltaM(dataSource, 
                "--recipient_table=delta_m_recipient",
                "--outbox_table=delta_m_outbox");
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Очищаем созданные таблицы после каждого теста
//        try (Connection connection = dataSource.getConnection();
//             Statement statement = connection.createStatement()) {
//            statement.execute("DROP TABLE IF EXISTS delta_m_changelog_table CASCADE");
//            statement.execute("DROP TABLE IF EXISTS schema_initialization_lock CASCADE");
//            statement.execute("DROP TABLE IF EXISTS outbox_message CASCADE");
//            statement.execute("DROP TABLE IF EXISTS schema_version CASCADE");
//        }
    }

    @Test
    void testExecuteCreatesRequiredTables() throws SQLException {
        // Проверяем, что база данных действительно PostgreSQL
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            assertTrue(productName.toLowerCase().contains("postgresql"), 
                "Database should be PostgreSQL, but was: " + productName);
        }

        // Выполняем основной метод
        assertDoesNotThrow(() -> deltaM.init());

        // Проверяем, что таблица recipient была создана
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                "SELECT table_name FROM information_schema.tables " +
                "WHERE table_name = 'delta_m_recipient' AND table_schema = 'delta_m'"
            );
            assertTrue(resultSet.next(), "Table 'recipient' should be created");
        }

        // Проверяем, что таблица outbox была создана
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                "SELECT table_name FROM information_schema.tables " +
                "WHERE table_name = 'delta_m_outbox' AND table_schema = 'delta_m'"
            );
            assertTrue(resultSet.next(), "Table 'outbox' should be created");
        }

        // Проверяем, что индекс на created_at был создан
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                "SELECT indexname FROM pg_indexes " +
                "WHERE tablename = 'delta_m_outbox' AND schemaname = 'delta_m' " +
                "AND indexname = 'idx_delta_m_outbox_created_at'"
            );
            assertTrue(resultSet.next(), "Index 'idx_delta_m_outbox_created_at' should be created");
        }

//        // Проверяем, что все необходимые таблицы были созданы
//        try (Connection connection = dataSource.getConnection();
//             Statement statement = connection.createStatement()) {
//
//            // Проверяем таблицу delta_m_changelog_table
//            ResultSet resultSet = statement.executeQuery(
//                "SELECT table_name FROM information_schema.tables " +
//                "WHERE table_name = 'delta_m_changelog_table' AND table_schema = 'public'"
//            );
//            assertTrue(resultSet.next(), "Table 'delta_m_changelog_table' should be created");
//
//            // Проверяем таблицу schema_initialization_lock
//            resultSet = statement.executeQuery(
//                "SELECT table_name FROM information_schema.tables " +
//                "WHERE table_name = 'schema_initialization_lock' AND table_schema = 'public'"
//            );
//            assertTrue(resultSet.next(), "Table 'schema_initialization_lock' should be created");
//
//            // Проверяем таблицу outbox_message
//            resultSet = statement.executeQuery(
//                "SELECT table_name FROM information_schema.tables " +
//                "WHERE table_name = 'outbox_message' AND table_schema = 'public'"
//            );
//            assertTrue(resultSet.next(), "Table 'outbox_message' should be created");
//
//            // Проверяем таблицу schema_version
//            resultSet = statement.executeQuery(
//                "SELECT table_name FROM information_schema.tables " +
//                "WHERE table_name = 'schema_version' AND table_schema = 'public'"
//            );
//            assertTrue(resultSet.next(), "Table 'schema_version' should be created");
//        }
    }

    @Disabled
    @Test
    void testExecuteMultipleTimesIsIdempotent() throws SQLException {
        // Первый запуск должен пройти успешно
        assertDoesNotThrow(() -> deltaM.init());
        
        // Второй запуск также должен пройти успешно (idempotent)
        assertDoesNotThrow(() -> deltaM.init());
        
        // Проверяем, что все таблицы все еще существуют
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            ResultSet resultSet = statement.executeQuery(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_name IN ('delta_m_changelog_table', 'schema_initialization_lock', 'outbox_message', 'schema_version') " +
                "AND table_schema = 'public'"
            );
            assertTrue(resultSet.next());
            assertEquals(4, resultSet.getInt(1), "All 4 tables should still exist");
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
            assertTrue(productName.toLowerCase().contains("postgresql"), 
                "Database should be PostgreSQL, but was: " + productName);
        }
    }

    @Disabled
    @Test
    void testTableStructure() throws Exception {
        // Выполняем основной метод
        deltaM.init();
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Проверяем структуру таблицы delta_m_changelog_table
            ResultSet resultSet = statement.executeQuery(
                "SELECT column_name, data_type FROM information_schema.columns " +
                "WHERE table_name = 'delta_m_changelog_table' AND table_schema = 'public' " +
                "ORDER BY ordinal_position"
            );
            
            assertTrue(resultSet.next());
            assertEquals("id", resultSet.getString("column_name"));
            assertEquals("integer", resultSet.getString("data_type"));
            
            assertTrue(resultSet.next());
            assertEquals("version", resultSet.getString("column_name"));
            assertEquals("character varying", resultSet.getString("data_type"));
            
            assertTrue(resultSet.next());
            assertEquals("applied_at", resultSet.getString("column_name"));
            assertEquals("timestamp with time zone", resultSet.getString("data_type"));
            
            assertTrue(resultSet.next());
            assertEquals("description", resultSet.getString("column_name"));
            assertEquals("text", resultSet.getString("data_type"));
        }
    }

    @Disabled
    @Test
    void testPostgreSQLSpecificFeatures() throws Exception {
        // Выполняем основной метод
        deltaM.init();
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Проверяем, что таблицы созданы в правильной схеме (public)
            ResultSet resultSet = statement.executeQuery(
                "SELECT schemaname, tablename FROM pg_tables " +
                "WHERE tablename IN ('delta_m_changelog_table', 'schema_initialization_lock') " +
                "ORDER BY tablename"
            );
            
            assertTrue(resultSet.next());
            assertEquals("public", resultSet.getString("schemaname"));
            assertEquals("delta_m_changelog_table", resultSet.getString("tablename"));
            
            assertTrue(resultSet.next());
            assertEquals("public", resultSet.getString("schemaname"));
            assertEquals("schema_initialization_lock", resultSet.getString("tablename"));
        }
    }
}
