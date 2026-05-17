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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Provides the version string for FigTree. VERSION and EXTRA_TAG are parsed
 * automatically from the Git tag embedded in revision.txt at build time.
 *
 * Tag format examples:
 *   v1.5.0          → VERSION="1.5.0"  EXTRA_TAG=""
 *   v1.5.0-beta1    → VERSION="1.5.0"  EXTRA_TAG="-beta1"
 *
 * Commits made after a tag are identified by the git-describe suffix
 * (-N-gabcdef) which is stripped; EXTRA_TAG remains empty for those.
 *
 * @author Andrew Rambaut
 */
public class FigTreeVersion {

    public static final String DATES = "2006-2026";

    // Parsed lazily from revision.txt on first access.
    private static String parsedVersion = null;
    private static String parsedExtraTag = null;

    private static void ensureParsed() {
        if (parsedVersion != null) {
            return;
        }
        String[] parts = parseTag(readRevisionLine());
        parsedVersion = parts[0];
        parsedExtraTag = parts[1];
    }

    /** The x.y.z version number derived from the Git tag (e.g. "1.5.0"). */
    public static String getVersion() {
        ensureParsed();
        return parsedVersion;
    }

    /** The pre-release suffix derived from the Git tag (e.g. "-beta1"), or "". */
    public static String getExtraTag() {
        ensureParsed();
        return parsedExtraTag;
    }

    /** True when the tag contains a pre-release suffix. */
    public static boolean isPreRelease() {
        return !getExtraTag().isEmpty();
    }

    /** Full version string, e.g. "v1.5.0" or "v1.5.0-beta1". */
    public static String getVersionString() {
        return "v" + getVersion() + getExtraTag();
    }

    /** URL to the GitHub release for this tag. */
    public static String getBuildString() {
        return "https://github.com/rambaut/figtree/releases/tag/v"
                + getVersion() + getExtraTag();
    }

    /**
     * Raw Git describe string from revision.txt (e.g. "v1.5.0-beta1").
     * "-dirty" suffix is stripped.
     */
    public static String getRevision() {
        return readRevisionLine();
    }

    // ── internal helpers ──────────────────────────────────────────────────

    private static String readRevisionLine() {
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
            String line = lines.get(1).trim();
            if (line.endsWith("-dirty")) {
                line = line.substring(0, line.length() - "-dirty".length());
            }
            return line;
        } catch (IOException e) {
            return "unknown";
        }
    }

    /**
     * Parses a git-describe string into [version, extraTag].
     *
     * "v1.5.0"               → ["1.5.0", ""]
     * "v1.5.0-beta1"         → ["1.5.0", "-beta1"]
     * "v1.5.0-beta1-3-gabc"  → ["1.5.0", "-beta1"]   (commit offset stripped)
     * "v1.5.0-3-gabc"        → ["1.5.0", ""]          (commit offset only)
     */
    private static String[] parseTag(String tag) {
        if (tag.startsWith("v")) {
            tag = tag.substring(1);
        }
        // Group 1: x.y.z version
        // Group 3: pre-release suffix starting with a letter (e.g. -beta1)
        // Group 4: optional git commit offset (-N-gabcdef), discarded
        Pattern p = Pattern.compile(
                "^(\\d+\\.\\d+\\.\\d+)((-[a-zA-Z]\\w*)(-\\d+-g[0-9a-f]+)?|-\\d+-g[0-9a-f]+)?$"
        );
        Matcher m = p.matcher(tag);
        if (m.matches()) {
            String version = m.group(1);
            String extraTag = m.group(3) != null ? m.group(3) : "";
            return new String[]{ version, extraTag };
        }
        // Fallback for unrecognised formats (e.g. plain commit hash)
        return new String[]{ tag, "" };
    }
}
