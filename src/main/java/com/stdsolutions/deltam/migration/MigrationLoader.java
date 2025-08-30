package com.stdsolutions.deltam.migration;

import com.stdsolutions.deltam.MigrationStep;
import com.stdsolutions.deltam.metadata.DatabaseType;
import com.stdsolutions.deltam.options.DamsOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class MigrationLoader {

    private static final Pattern MIGRATION_FILE_PATTERN = Pattern.compile("(\\d+)__(.+)\\.sql");

    private final DamsOptions options;
    private final DatabaseType databaseType;

    public MigrationLoader(final DamsOptions options, final DatabaseType databaseType) {
        this.options = options;
        this.databaseType = databaseType;
    }

    public List<MigrationStep> steps() throws IOException {
        List<MigrationStep> migrations = new ArrayList<>();

        String migrationsPath = buildMigrationPath();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL migrationsUrl = classLoader.getResource(migrationsPath);

        if (migrationsUrl == null) {
            return migrations;
        }

        String[] migrationFiles = findMigrationFiles(migrationsUrl, migrationsPath);

        for (String fileName : migrationFiles) {
            Matcher matcher = MIGRATION_FILE_PATTERN.matcher(fileName);
            if (matcher.matches()) {
                String migrationNumber = matcher.group(1);
                String migrationName = matcher.group(2);
                String content = loadMigrationContent(classLoader, migrationsPath, fileName);
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

    private String[] findMigrationFiles(URL migrationsUrl, String migrationsPath) {
        try {
            URI uri = migrationsUrl.toURI();
            List<String> migrationFiles = new ArrayList<>();

            if ("jar".equals(uri.getScheme())) {
                // Running from JAR
                migrationFiles = findMigrationFilesInJar(migrationsUrl, migrationsPath);
            } else {
                // Running from filesystem (development)
                migrationFiles = findMigrationFilesInFileSystem(uri, migrationsPath);
            }

            return migrationFiles.stream()
                .filter(filename -> MIGRATION_FILE_PATTERN.matcher(filename).matches())
                .sorted()
                .toArray(String[]::new);

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Failed to discover migration files", e);
        }
    }

    private List<String> findMigrationFilesInJar(URL resource, String migrationsPath) throws IOException {
        List<String> files = new ArrayList<>();
        JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();

        try (JarFile jarFile = jarConnection.getJarFile()) {
            String pathWithSlash = migrationsPath.endsWith("/") ? migrationsPath : migrationsPath + "/";

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith(pathWithSlash) &&
                    entryName.endsWith(".sql") &&
                    !entry.isDirectory()) {

                    String fileName = entryName.substring(pathWithSlash.length());
                    if (!fileName.contains("/")) { // Only direct files, not subdirectories
                        files.add(fileName);
                    }
                }
            }
        }

        return files;
    }

    private List<String> findMigrationFilesInFileSystem(URI uri, String migrationsPath) throws IOException {
        List<String> files = new ArrayList<>();
        Path migrationDir = Paths.get(uri);

        if (Files.exists(migrationDir) && Files.isDirectory(migrationDir)) {
            try (Stream<Path> paths = Files.list(migrationDir)) {
                files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
            }
        }

        return files;
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
        String basePath = options.migrationPath();
        String dbTypePath = databaseType.name().toLowerCase();
        return basePath + "/" + dbTypePath;
    }

}