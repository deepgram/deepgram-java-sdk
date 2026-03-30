.PHONY: lint format build test check test-all test-integration compile-examples

# Lint: check formatting of custom (non-generated) files
lint:
	./gradlew spotlessCheck

# Format: apply formatting to custom (non-generated) files
format:
	./gradlew spotlessApply

# Build: compile without tests
build:
	./gradlew build -x test

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

# Compile all examples
compile-examples:
	./gradlew compileExamples
