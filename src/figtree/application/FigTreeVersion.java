/*
 * FigTreeVersion.java
 *
 * Copyright (C) 2006-2025 Andrew Rambaut
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package figtree.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides the version string for FigTree. The VERSION constant is updated
 * manually for each release. The revision string is populated automatically
 * at build time from the Git tag/commit via revision.txt.
 *
 * @author Andrew Rambaut
 */
public class FigTreeVersion {

    public static final String VERSION = "1.5.0";
    public static final String DATES = "2006-2025";
    public static final boolean IS_PRERELEASE = false;

    /**
     * Returns the full version string, e.g. "v1.5.0" for a release or
     * "v1.5.0 Prerelease #abc1234" for a pre-release build.
     */
    public static String getVersionString() {
        return "v" + VERSION + (IS_PRERELEASE ? " Prerelease #" + getRevision() : "");
    }

    /**
     * Returns a URL to the GitHub release tag for this version.
     */
    public static String getBuildString() {
        return "https://github.com/rambaut/figtree/releases/tag/v" + VERSION;
    }

    /**
     * Reads the Git commit/tag written into revision.txt at build time.
     * Returns "unknown" if the resource is not found.
     */
    public static String getRevision() {
        try (InputStream in = FigTreeVersion.class.getResourceAsStream("/revision.txt")) {
            if (in == null) {
                return "unknown";
            }
            List<String> lines = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.toList());
            if (lines.size() < 2) {
                return "unknown";
            }
            String revision = lines.get(1);
            if (revision.endsWith("-dirty")) {
                revision = revision.substring(0, revision.length() - "-dirty".length());
            }
            return revision;
        } catch (IOException e) {
            return "unknown";
        }
    }
}
