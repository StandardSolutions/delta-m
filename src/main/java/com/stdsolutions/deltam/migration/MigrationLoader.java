package com.stdsolutions.deltam.migration;

import com.stdsolutions.deltam.MigrationStep;
import com.stdsolutions.deltam.files.FileList;
import com.stdsolutions.deltam.files.FileListOf;
import com.stdsolutions.deltam.files.MigrationPath;
import com.stdsolutions.deltam.files.filter.SqlFilteredFileList;
import com.stdsolutions.deltam.files.path.SafePath;
import com.stdsolutions.deltam.files.path.UnPrefixedPath;
import com.stdsolutions.deltam.metadata.DatabaseType;
import com.stdsolutions.deltam.options.MigrationOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MigrationLoader {

    private static final Pattern MIGRATION_FILE_PATTERN = Pattern.compile("(\\d+)__(.+)\\.sql");

    private final MigrationOptions options;
    private final DatabaseType databaseType;

    public MigrationLoader(final MigrationOptions options, final DatabaseType databaseType) {
        this.options = options;
        this.databaseType = databaseType;
    }

    public List<MigrationStep> steps() throws IOException, URISyntaxException {
        List<MigrationStep> migrations = new ArrayList<>();

        String migrationsPath = buildMigrationPath();

        MigrationPath dbSpecificPath = createDbSpecificPath(migrationsPath);
        FileList fileList = new SqlFilteredFileList(new FileListOf(dbSpecificPath).value());
        List<String> migrationFiles = fileList.values();

        for (String fileName : migrationFiles) {
            Matcher matcher = MIGRATION_FILE_PATTERN.matcher(fileName);
            if (matcher.matches()) {
                String migrationNumber = matcher.group(1);
                String migrationName = matcher.group(2);
                String content = loadMigrationContent(migrationsPath, fileName);
                String processedContent = processTemplate(content, options);

                migrations.add(new SqlMigrationStep(
                    migrationNumber + "_" + migrationName,
                    migrationName.replace("_", " "),
                    processedContent
                ));
            }
        }

        return migrations;
    }


    private String loadMigrationContent(String migrationsPath, String fileName) throws IOException {
        MigrationPath originalPath = options.migrationPath();
        
        if (originalPath.isFileSystem()) {
            // Load from real filesystem
            Path filePath = Paths.get(migrationsPath, fileName);
            if (!Files.exists(filePath)) {
                throw new IOException("Migration file not found: " + filePath.toAbsolutePath());
            }
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } else {
            // Load from classpath resources
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String resourcePath = Paths.get(migrationsPath, fileName).toString().replace('\\', '/');
            try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
                if (inputStream == null) {
                    throw new IOException("Migration file not found: " + resourcePath);
                }
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    private String processTemplate(String content, MigrationOptions options) {
        Pattern namePattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = namePattern.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String name = matcher.group(1);
            String replacement = options.tableName(name);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private String buildMigrationPath() {
        String basePath = options.migrationPath().toString();
        String dbTypePath = databaseType.name().toLowerCase();
        return basePath + "/" + dbTypePath;
    }
    
    private MigrationPath createDbSpecificPath(String migrationsPath) {
        // Preserve the original prefix from the migration options
        MigrationPath originalPath = options.migrationPath();
        
        if (originalPath.isFileSystem()) {
            // For filesystem paths, create a new path with filesystem: prefix
            return new SafePath(new UnPrefixedPath("filesystem:" + migrationsPath));
        } else if (originalPath.isClasspath()) {
            // For classpath paths, create a new path with classpath: prefix  
            return new SafePath(new UnPrefixedPath("classpath:" + migrationsPath));
        } else {
            // For unprefixed paths, create without prefix (backward compatibility)
            return new SafePath(new UnPrefixedPath(migrationsPath));
        }
    }

}