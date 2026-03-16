# Contributing to Deepgram Java SDK

We welcome contributions! This document outlines the process for contributing to the Deepgram Java SDK.

## Development Setup

### Prerequisites

- Java 11 or higher (Java 17 recommended)
- Git
- A Deepgram API key ([sign up here](https://console.deepgram.com/signup))

### Getting Started

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/deepgram-java-sdk.git
   cd deepgram-java-sdk
   ```

3. Verify the build works:
   ```bash
   ./gradlew build -x test -x spotlessCheck
   ```

4. Set up your environment:
   ```bash
   export DEEPGRAM_API_KEY="your-api-key-here"
   ```

5. Set up the pre-commit hook:
   ```bash
   git config core.hooksPath .githooks
   ```

## Development Workflow

### Build Commands

This project uses a Makefile for common tasks:

```bash
make build             # Compile the project
make lint              # Check code formatting (Spotless)
make format            # Auto-format code (Spotless)
make test              # Run unit tests (excludes integration)
make check             # Lint + build + unit tests (pre-commit)
make test-all          # Full test suite including integration
make test-integration  # Integration tests only
```

### Running Tests

Unit tests (no API key required):
```bash
./gradlew unitTest
```

Integration tests (requires `DEEPGRAM_API_KEY`):
```bash
export DEEPGRAM_API_KEY="your-api-key-here"
./gradlew integrationTest
```

All tests:
```bash
./gradlew test
```

Run tests with verbose output:
```bash
./gradlew test --info
```

### Code Style

This project uses [Spotless](https://github.com/diffplug/spotless) with [Google Java Format](https://github.com/google/google-java-format) for consistent code formatting.

Check formatting:
```bash
./gradlew spotlessCheck
```

Auto-format code:
```bash
./gradlew spotlessApply
```

The pre-commit hook will run `make check` automatically, which includes the Spotless lint check. Make sure to format your code before committing:

```bash
make format
```

### Making Changes

1. Create a new branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes and commit them using [Conventional Commits](https://www.conventionalcommits.org/):
   ```bash
   git commit -m "feat: add new feature"
   git commit -m "fix: resolve issue with authentication"
   git commit -m "docs: update README examples"
   ```

   Common commit types:
   - `feat`: New feature
   - `fix`: Bug fix
   - `docs`: Documentation changes
   - `test`: Adding or updating tests
   - `refactor`: Code refactoring
   - `chore`: Maintenance tasks

3. Push to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

4. Open a Pull Request

## Pull Request Guidelines

- Keep PRs focused on a single feature or fix
- Include tests for new functionality
- Update documentation as needed
- Ensure all tests pass (`make check`)
- Follow the existing code style (run `make format`)
- Write clear commit messages

## Code Generation

This SDK is generated using [Fern](https://buildwithfern.com) from Deepgram's OpenAPI specification. Generated code should not be edited directly.

If you need to make changes to the generated SDK:
1. Update the OpenAPI spec in the [deepgram-api-specs](https://github.com/deepgram/deepgram-api-specs) repository
2. Regenerate the SDK using Fern

For documentation or example changes, those can be made directly to this repository.

## Project Structure

```
deepgram-java-sdk/
  src/
    main/java/
      core/                  # HTTP client, options, utilities
      errors/                # Error types
      resources/             # API resource clients
        agent/               # Voice Agent API
        auth/                # Authentication API
        listen/              # Speech-to-Text API (REST + WebSocket)
        manage/              # Management API
        read/                # Text Intelligence API
        selfhosted/          # Self-Hosted API
        speak/               # Text-to-Speech API (REST + WebSocket)
      types/                 # Shared request/response types
      DeepgramApiClient.java # Main sync client
      AsyncDeepgramApiClient.java # Main async client
    test/java/               # Test files
  build.gradle               # Build configuration
  Makefile                   # Build automation
```

## Reporting Issues

- Use the [GitHub issue tracker](https://github.com/deepgram/deepgram-java-sdk/issues)
- Check if the issue already exists before creating a new one
- Provide clear reproduction steps
- Include relevant error messages and logs
- Include your Java version (`java -version`)

## Getting Help

- Join our [Discord community](https://discord.gg/deepgram)
- Check the [documentation](https://developers.deepgram.com)
- Email [support@deepgram.com](mailto:support@deepgram.com)

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
