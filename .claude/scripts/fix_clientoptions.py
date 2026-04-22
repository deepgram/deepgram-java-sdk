#!/usr/bin/env python3

import json
import re
import sys
from pathlib import Path


CLIENT_OPTIONS_PATH = Path("src/main/java/com/deepgram/core/ClientOptions.java")
CLIENT_OPTIONS_BAK_PATH = Path("src/main/java/com/deepgram/core/ClientOptions.java.bak")
BUILD_GRADLE_PATH = Path("build.gradle")
METADATA_PATH = Path(".fern/metadata.json")
SDK_NAME = "com.deepgram:deepgram-java-sdk"


def load_sdk_version(text: str) -> str:
    if CLIENT_OPTIONS_BAK_PATH.exists():
        bak_text = CLIENT_OPTIONS_BAK_PATH.read_text()
        match = re.search(r'put\("X-Fern-SDK-Version",\s*"([^"]+)"\)', bak_text)
        if match:
            return match.group(1)

        match = re.search(r'put\("User-Agent",\s*"[^"]*/([^"]+)"\)', bak_text)
        if match:
            return match.group(1)

    if BUILD_GRADLE_PATH.exists():
        build_gradle = BUILD_GRADLE_PATH.read_text()
        match = re.search(r"^version\s*=\s*'([^']+)'", build_gradle, re.MULTILINE)
        if match:
            return match.group(1)

    match = re.search(r'put\("X-Fern-SDK-Version",\s*"([^"]+)"\)', text)
    if match:
        return match.group(1)

    match = re.search(r'put\("User-Agent",\s*"[^"]*/([^"]+)"\)', text)
    if match:
        return match.group(1)

    if METADATA_PATH.exists():
        metadata = json.loads(METADATA_PATH.read_text())
        sdk_version = metadata.get("sdkVersion")
        if sdk_version:
            return sdk_version

    raise RuntimeError("Unable to determine SDK version for ClientOptions.java")


def replace_header_line(
    text: str, key: str, value: str, add_release_please: bool
) -> str:
    pattern = re.compile(
        rf'^(?P<indent>\s*)put\("{re.escape(key)}",\s*"[^"]*"\);(?:\s*// x-release-please-version)?\s*$',
        re.MULTILINE,
    )

    def repl(match: re.Match[str]) -> str:
        suffix = " // x-release-please-version" if add_release_please else ""
        return f'{match.group("indent")}put("{key}", "{value}");{suffix}'

    updated_text, count = pattern.subn(repl, text, count=1)
    if count != 1:
        raise RuntimeError(f"Unable to locate header line for {key}")
    return updated_text


def main() -> int:
    if not CLIENT_OPTIONS_PATH.exists():
        raise RuntimeError(f"File not found: {CLIENT_OPTIONS_PATH}")

    original_text = CLIENT_OPTIONS_PATH.read_text()
    sdk_version = load_sdk_version(original_text)

    updated_text = original_text
    updated_text = replace_header_line(
        updated_text,
        "User-Agent",
        f"{SDK_NAME}/{sdk_version}",
        add_release_please=True,
    )
    updated_text = replace_header_line(
        updated_text,
        "X-Fern-SDK-Name",
        SDK_NAME,
        add_release_please=False,
    )
    updated_text = replace_header_line(
        updated_text,
        "X-Fern-SDK-Version",
        sdk_version,
        add_release_please=True,
    )

    if updated_text != original_text:
        CLIENT_OPTIONS_PATH.write_text(updated_text)
        print(f"Updated {CLIENT_OPTIONS_PATH} to {SDK_NAME}/{sdk_version}")
    else:
        print(
            f"{CLIENT_OPTIONS_PATH} already has the expected Deepgram header constants"
        )

    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"Error: {exc}", file=sys.stderr)
        raise SystemExit(1)
