package com.deepgram;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;

/** Compile-smoke tests for Java snippets embedded in README.md. */
class ReadmeSnippetsCompileTest {

    private static final Path README_PATH = Paths.get("README.md");

    private static final List<String> COMMON_IMPORTS = Arrays.asList(
            "import com.deepgram.*;",
            "import com.deepgram.core.*;",
            "import com.deepgram.core.transport.*;",
            "import com.deepgram.errors.*;",
            "import com.deepgram.resources.agent.v1.types.*;",
            "import com.deepgram.resources.agent.v1.websocket.*;",
            "import com.deepgram.resources.listen.v1.media.requests.*;",
            "import com.deepgram.resources.listen.v1.media.types.*;",
            "import com.deepgram.resources.listen.v1.types.*;",
            "import com.deepgram.resources.listen.v1.websocket.*;",
            "import com.deepgram.resources.read.v1.text.requests.*;",
            "import com.deepgram.resources.speak.v1.audio.requests.*;",
            "import com.deepgram.resources.speak.v1.types.*;",
            "import com.deepgram.resources.speak.v1.websocket.*;",
            "import com.deepgram.sagemaker.*;",
            "import com.deepgram.types.*;",
            "import java.io.*;",
            "import java.nio.file.*;",
            "import java.util.*;",
            "import java.util.concurrent.*;",
            "import java.util.function.*;",
            "import okio.*;",
            "import okhttp3.*;");

    @Test
    void readmeJavaExamplesCompile() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "README snippet compilation requires a JDK, not a JRE");

        List<Snippet> snippets = extractCompilableJavaSnippets();
        assertFalse(snippets.isEmpty(), "Expected to find Java snippets in README.md");

        Path tempDir = Files.createTempDirectory("readme-snippets");
        try {
            Map<String, Snippet> snippetsByClassName = new HashMap<>();
            List<Path> sourceFiles = new ArrayList<>();
            for (int i = 0; i < snippets.size(); i++) {
                String className = String.format("ReadmeSnippet%02d", i + 1);
                Snippet snippet = snippets.get(i);
                snippetsByClassName.put(className, snippet);

                Path sourceFile = tempDir.resolve(className + ".java");
                Files.writeString(sourceFile, renderSnippetSource(className, snippet), StandardCharsets.UTF_8);
                sourceFiles.add(sourceFile);
            }

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            try (StandardJavaFileManager fileManager =
                    compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
                Iterable<? extends JavaFileObject> compilationUnits =
                        fileManager.getJavaFileObjectsFromPaths(sourceFiles);

                List<String> options = Arrays.asList(
                        "--release",
                        "11",
                        "-classpath",
                        System.getProperty("java.class.path"),
                        "-d",
                        tempDir.toString());

                Boolean success = compiler
                        .getTask(null, fileManager, diagnostics, options, null, compilationUnits)
                        .call();

                if (!Boolean.TRUE.equals(success)) {
                    fail(formatDiagnostics(diagnostics.getDiagnostics(), snippetsByClassName));
                }
            }
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private static List<Snippet> extractCompilableJavaSnippets() throws IOException {
        List<String> lines = Files.readAllLines(README_PATH, StandardCharsets.UTF_8);
        List<Snippet> snippets = new ArrayList<>();

        String currentH2 = "";
        String currentH3 = "";
        boolean inJavaFence = false;
        int snippetStartLine = -1;
        StringBuilder snippetBody = new StringBuilder();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (!inJavaFence) {
                if (line.startsWith("## ")) {
                    currentH2 = line.substring(3).trim();
                    currentH3 = "";
                } else if (line.startsWith("### ")) {
                    currentH3 = line.substring(4).trim();
                }

                if (line.equals("```java")) {
                    inJavaFence = true;
                    snippetStartLine = i + 1;
                    snippetBody.setLength(0);
                }
                continue;
            }

            if (line.equals("```")) {
                inJavaFence = false;
                if (!"Complete SDK Reference".equals(currentH2)) {
                    String name = currentH3.isEmpty() ? currentH2 : currentH2 + " / " + currentH3;
                    snippets.add(new Snippet(name, snippetStartLine, snippetBody.toString()));
                }
                continue;
            }

            snippetBody.append(line).append('\n');
        }

        return snippets;
    }

    private static String renderSnippetSource(String className, Snippet snippet) {
        LinkedHashSet<String> imports = new LinkedHashSet<>(COMMON_IMPORTS);
        StringBuilder body = new StringBuilder();

        for (String line : snippet.code().split("\\R", -1)) {
            if (line.startsWith("import ")) {
                imports.add(line.endsWith(";") ? line : line + ";");
            } else {
                body.append(line).append('\n');
            }
        }

        return imports.stream().collect(Collectors.joining("\n"))
                + "\n\n"
                + "public final class "
                + className
                + " {\n"
                + "    private static final DeepgramClient client =\n"
                + "            DeepgramClient.builder().apiKey(\"test-api-key\").build();\n"
                + "    private static final AsyncDeepgramClient asyncClient =\n"
                + "            AsyncDeepgramClient.builder().apiKey(\"test-api-key\").build();\n"
                + "    private static final ListenV1RequestUrl request = ListenV1RequestUrl.builder()\n"
                + "            .url(\"https://example.com/audio.wav\")\n"
                + "            .build();\n"
                + "    private static final byte[] body = new byte[0];\n"
                + "\n"
                + "    private static final class MyCustomTransport implements DeepgramTransport {\n"
                + "        private MyCustomTransport(String url, Map<String, String> headers) {}\n"
                + "\n"
                + "        @Override\n"
                + "        public CompletableFuture<Void> sendBinary(byte[] data) {\n"
                + "            return CompletableFuture.completedFuture(null);\n"
                + "        }\n"
                + "\n"
                + "        @Override\n"
                + "        public CompletableFuture<Void> sendText(String data) {\n"
                + "            return CompletableFuture.completedFuture(null);\n"
                + "        }\n"
                + "\n"
                + "        @Override\n"
                + "        public void onTextMessage(Consumer<String> listener) {}\n"
                + "\n"
                + "        @Override\n"
                + "        public void onBinaryMessage(Consumer<byte[]> listener) {}\n"
                + "\n"
                + "        @Override\n"
                + "        public void onOpen(Runnable listener) {}\n"
                + "\n"
                + "        @Override\n"
                + "        public void onError(Consumer<Throwable> listener) {}\n"
                + "\n"
                + "        @Override\n"
                + "        public void onClose(CloseListener listener) {}\n"
                + "\n"
                + "        @Override\n"
                + "        public boolean isOpen() {\n"
                + "            return true;\n"
                + "        }\n"
                + "\n"
                + "        @Override\n"
                + "        public void close() {}\n"
                + "    }\n"
                + "\n"
                + "    public static void compileOnly() throws Exception {\n"
                + indent(body.toString(), 8)
                + "    }\n"
                + "}\n";
    }

    private static String formatDiagnostics(
            List<Diagnostic<? extends JavaFileObject>> diagnostics, Map<String, Snippet> snippetsByClassName) {
        StringBuilder message = new StringBuilder("README Java snippets failed to compile:\n");
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
            JavaFileObject source = diagnostic.getSource();
            String className = source == null
                    ? "unknown"
                    : Path.of(source.toUri()).getFileName().toString().replace(".java", "");
            Snippet snippet = snippetsByClassName.get(className);

            if (snippet != null) {
                message.append("- ")
                        .append(snippet.name())
                        .append(" (README line ")
                        .append(snippet.startLine())
                        .append(")\n");
            } else {
                message.append("- ").append(className).append('\n');
            }

            message.append("  ")
                    .append(diagnostic.getKind())
                    .append(": line ")
                    .append(diagnostic.getLineNumber())
                    .append(": ")
                    .append(diagnostic.getMessage(null))
                    .append('\n');
        }
        return message.toString();
    }

    private static String indent(String text, int spaces) {
        String prefix = " ".repeat(spaces);
        return Arrays.stream(text.split("\\R", -1))
                .map(line -> line.isEmpty() ? "" : prefix + line)
                .collect(Collectors.joining("\n", "", "\n"));
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (java.util.stream.Stream<Path> stream = Files.walk(path)) {
            for (Path current : stream.sorted((a, b) -> b.compareTo(a)).collect(Collectors.toList())) {
                Files.deleteIfExists(current);
            }
        }
    }

    private static final class Snippet {
        private final String name;
        private final int startLine;
        private final String code;

        private Snippet(String name, int startLine, String code) {
            this.name = name;
            this.startLine = startLine;
            this.code = code;
        }

        private String name() {
            return name;
        }

        private int startLine() {
            return startLine;
        }

        private String code() {
            return code;
        }
    }
}
