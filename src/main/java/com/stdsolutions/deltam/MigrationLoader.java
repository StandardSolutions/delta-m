package com.stdsolutions.deltam;

import com.stdsolutions.deltam.options.DamsOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MigrationLoader {

    private static final Pattern MIGRATION_FILE_PATTERN = Pattern.compile("(\\d+)__(.+)\\.sql");

    private final DamsOptions options;

    public MigrationLoader(final DamsOptions options) {
        this.options = options;
    }

    public List<MigrationStep> steps() throws IOException {
        List<MigrationStep> migrations = new ArrayList<>();
        
        String migrationsPath = options.migrationPath();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL migrationsUrl = classLoader.getResource(migrationsPath);
        
        if (migrationsUrl == null) {
            return migrations;
        }

        String[] migrationFiles = findMigrationFiles(classLoader, migrationsPath);
        
        Arrays.sort(migrationFiles); // Sort by filename to ensure order
        
        for (String fileName : migrationFiles) {
            Matcher matcher = MIGRATION_FILE_PATTERN.matcher(fileName);
            if (matcher.matches()) {
                String migrationNumber = matcher.group(1);
                String migrationName = matcher.group(2);
                String content = loadMigrationContent(classLoader, migrationsPath, fileName);
                String processedContent = processTemplate(content, options);
                
                migrations.add(new FileMigrationStep(
                    migrationNumber + "_" + migrationName,
                    migrationName.replace("_", " "),
                    processedContent,
                    options
                ));
            }
        }
        
        return migrations;
    }

    private String[] findMigrationFiles(ClassLoader classLoader, String migrationsPath) {
        List<String> files = new ArrayList<>();
        
        // Try to find migration files by checking known patterns
        for (int i = 1; i <= 100; i++) {
            String fileName = String.format("%03d__", i);
            String[] possibleSuffixes = {
                "create_changelog_table.sql",
                "create_recipient_table.sql", 
                "create_outbox_table.sql",
                "create_message_type_table.sql",
                "create_recipient_message_type_table.sql"
            };
            
            for (String suffix : possibleSuffixes) {
                String fullFileName = fileName + suffix;
                if (classLoader.getResource(Paths.get(migrationsPath, fullFileName).toString().replace('\\', '/')) != null) {
                    files.add(fullFileName);
                }
            }
        }
        
        return files.toArray(new String[0]);
    }

    private String loadMigrationContent(ClassLoader classLoader, String migrationsPath, String fileName) throws IOException {
        String resourcePath = Paths.get(migrationsPath, fileName).toString().replace('\\', '/');
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Migration file not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String processTemplate(String content, DamsOptions options) {
        String result = content
                .replace("${CHANGELOG_TABLE}", options.changeLogTableName())
                .replace("${LOCK_TABLE}", options.lockTableName())
                .replace("${SCHEMA}", options.schema());
        
        // Process generic patterns like ${name}
        Pattern namePattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = namePattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String name = matcher.group(1);
            String replacement = options.tableName(name);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }

    private static class FileMigrationStep implements MigrationStep {
        private final String id;
        private final String description;
        private final String sql;

        public FileMigrationStep(String id, String description, String sql, DamsOptions options) {
            this.id = id;
            this.description = description;
            this.sql = sql;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public void execute(Connection connection) throws SQLException {
            try (var stmt = connection.createStatement()) {
                // Split SQL by semicolons and execute each statement
                String[] statements = sql.split(";");
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        }
    }
}