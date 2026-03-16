.PHONY: lint format build test check test-all test-integration

# Lint: check code formatting with Spotless
lint:
	./gradlew spotlessCheck

# Format: apply code formatting with Spotless
format:
	./gradlew spotlessApply

# Build: compile without tests or lint
build:
	./gradlew build -x test -x spotlessCheck

# Unit tests: exclude integration tests
test:
	./gradlew unitTest

# Pre-commit: lint, build, and unit test
check: lint build test

# Full suite: check + all tests (unit + integration)
test-all: check
	./gradlew test

# Integration tests only (requires DEEPGRAM_API_KEY)
test-integration:
	./gradlew integrationTest
