// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

final class CrashReportService {
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter
        .ofPattern("yyyyMMdd-HHmmss-SSS")
        .withZone(ZoneOffset.UTC);
    private static final int DEFAULT_MAX_REPORTS = 10;

    private final Path logsDirectory;
    private final BuildInfo buildInfo;
    private final Clock clock;
    private final int maxReports;

    CrashReportService() {
        this(AppPaths.logsDirectory(), BuildInfo.current(), Clock.systemUTC(), DEFAULT_MAX_REPORTS);
    }

    CrashReportService(Path logsDirectory, BuildInfo buildInfo, Clock clock, int maxReports) {
        this.logsDirectory = Objects.requireNonNull(logsDirectory, "logsDirectory");
        this.buildInfo = Objects.requireNonNull(buildInfo, "buildInfo");
        this.clock = Objects.requireNonNull(clock, "clock");
        if (maxReports < 1) throw new IllegalArgumentException("maxReports must be positive");
        this.maxReports = maxReports;
    }

    static CrashReportService installDefaultHandler() {
        CrashReportService service = new CrashReportService();
        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Optional<Path> report = service.writeCrash(thread, throwable);
            report.ifPresent(path -> System.err.println("HORSEBOUND crash report: " + path));
            if (previous != null) {
                previous.uncaughtException(thread, throwable);
            } else {
                throwable.printStackTrace(System.err);
            }
        });
        return service;
    }

    Optional<Path> writeCrash(Thread thread, Throwable throwable) {
        Objects.requireNonNull(thread, "thread");
        Objects.requireNonNull(throwable, "throwable");
        try {
            Files.createDirectories(logsDirectory);
            Instant now = clock.instant();
            Path report = uniqueReportPath(now);
            String content = buildReport(now, thread, throwable);
            Files.writeString(
                report,
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
            );
            rotateReports();
            return Optional.of(report);
        } catch (Exception reportingFailure) {
            System.err.println("HORSEBOUND could not write a crash report: " + reportingFailure.getMessage());
            return Optional.empty();
        }
    }

    Path logsDirectory() {
        return logsDirectory;
    }

    private Path uniqueReportPath(Instant now) {
        String base = "crash-" + FILE_TIME.format(now);
        Path candidate = logsDirectory.resolve(base + ".log");
        if (!Files.exists(candidate)) return candidate;
        return logsDirectory.resolve(base + "-" + UUID.randomUUID().toString().substring(0, 8) + ".log");
    }

    private String buildReport(Instant now, Thread thread, Throwable throwable) {
        StringWriter stackBuffer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackBuffer));

        Runtime runtime = Runtime.getRuntime();
        StringBuilder report = new StringBuilder(4096);
        report.append("HORSEBOUND LOCAL CRASH REPORT\n")
            .append("No telemetry was transmitted. This file stays on this device.\n\n")
            .append("Timestamp UTC: ").append(now).append('\n')
            .append("Build: ").append(buildInfo.diagnosticLabel()).append('\n')
            .append("Vendor: ").append(buildInfo.vendor()).append('\n')
            .append("Thread: ").append(thread.getName()).append(" (#").append(thread.threadId()).append(")\n")
            .append("OS: ").append(systemProperty("os.name")).append(' ')
            .append(systemProperty("os.version")).append(" [")
            .append(systemProperty("os.arch")).append("]\n")
            .append("Java: ").append(systemProperty("java.vendor")).append(' ')
            .append(systemProperty("java.version")).append('\n')
            .append("Processors: ").append(runtime.availableProcessors()).append('\n')
            .append("Memory max MiB: ").append(toMiB(runtime.maxMemory())).append('\n')
            .append("Memory total MiB: ").append(toMiB(runtime.totalMemory())).append('\n')
            .append("Memory free MiB: ").append(toMiB(runtime.freeMemory())).append('\n')
            .append("\nSTACK TRACE\n")
            .append(redactSensitivePaths(stackBuffer.toString()));
        return report.toString();
    }

    private void rotateReports() throws Exception {
        try (Stream<Path> files = Files.list(logsDirectory)) {
            List<Path> reports = files
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().startsWith("crash-"))
                .filter(path -> path.getFileName().toString().endsWith(".log"))
                .sorted(Comparator.comparingLong(CrashReportService::lastModified).reversed())
                .toList();
            for (int i = maxReports; i < reports.size(); i++) {
                Files.deleteIfExists(reports.get(i));
            }
        }
    }

    private static String redactSensitivePaths(String text) {
        String redacted = text;
        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isBlank()) {
            redacted = redacted.replace(userHome, "<USER_HOME>");
        }
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            redacted = redacted.replace(appData, "<APPDATA>");
        }
        return redacted;
    }

    private static String systemProperty(String key) {
        String value = System.getProperty(key);
        return value == null || value.isBlank() ? "unknown" : value;
    }

    private static long toMiB(long bytes) {
        return bytes / (1024L * 1024L);
    }

    private static long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (Exception ignored) {
            return Long.MIN_VALUE;
        }
    }
}
