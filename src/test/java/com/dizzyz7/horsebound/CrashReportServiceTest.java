// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrashReportServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void writesUsefulLocalReportAndRedactsHomePath() throws Exception {
        BuildInfo build = new BuildInfo("0.4.5", "abc123def456", AuthorInfo.CREATOR);
        CrashReportService service = new CrashReportService(
            tempDir.resolve("logs"),
            build,
            Clock.fixed(Instant.parse("2026-08-01T00:00:00Z"), ZoneOffset.UTC),
            10
        );
        String userHome = System.getProperty("user.home", "");
        RuntimeException failure = new RuntimeException("Could not read " + userHome + "/private-file");

        Optional<Path> reportPath = service.writeCrash(Thread.currentThread(), failure);

        assertTrue(reportPath.isPresent());
        String report = Files.readString(reportPath.orElseThrow());
        assertTrue(report.contains("HORSEBOUND 0.4.5 (abc123def456)"));
        assertTrue(report.contains("RuntimeException"));
        assertTrue(report.contains("<USER_HOME>/private-file") || userHome.isBlank());
        if (!userHome.isBlank()) assertFalse(report.contains(userHome));
        assertTrue(report.contains("No telemetry was transmitted"));
    }

    @Test
    void retainsOnlyConfiguredNumberOfCrashReports() throws Exception {
        CrashReportService service = new CrashReportService(
            tempDir.resolve("logs"),
            new BuildInfo("test", "local", AuthorInfo.CREATOR),
            Clock.fixed(Instant.parse("2026-08-01T00:00:00Z"), ZoneOffset.UTC),
            2
        );

        service.writeCrash(Thread.currentThread(), new IllegalStateException("one"));
        service.writeCrash(Thread.currentThread(), new IllegalStateException("two"));
        service.writeCrash(Thread.currentThread(), new IllegalStateException("three"));

        try (Stream<Path> reports = Files.list(service.logsDirectory())) {
            assertEquals(2, reports.filter(path -> path.getFileName().toString().endsWith(".log")).count());
        }
    }

    @Test
    void returnsEmptyInsteadOfThrowingWhenLogPathCannotBeCreated() throws Exception {
        Path fileInsteadOfDirectory = tempDir.resolve("not-a-directory");
        Files.writeString(fileInsteadOfDirectory, "occupied");
        CrashReportService service = new CrashReportService(
            fileInsteadOfDirectory.resolve("logs"),
            new BuildInfo("test", "local", AuthorInfo.CREATOR),
            Clock.systemUTC(),
            2
        );

        assertTrue(service.writeCrash(Thread.currentThread(), new RuntimeException("boom")).isEmpty());
    }
}
