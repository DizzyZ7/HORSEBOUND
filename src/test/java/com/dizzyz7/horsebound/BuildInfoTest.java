// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuildInfoTest {
    @Test
    void normalizesMissingManifestValuesForDevelopmentRuns() {
        BuildInfo info = new BuildInfo(null, " ", null);

        assertEquals("development", info.version());
        assertEquals("local", info.commit());
        assertEquals(AuthorInfo.CREATOR, info.vendor());
        assertEquals("HORSEBOUND development (local)", info.diagnosticLabel());
    }

    @Test
    void formatsPackagedVersionAndCommit() {
        BuildInfo info = new BuildInfo("0.4.5", "123456789abc", AuthorInfo.CREATOR);

        assertEquals("HORSEBOUND 0.4.5 (123456789abc)", info.diagnosticLabel());
    }
}
