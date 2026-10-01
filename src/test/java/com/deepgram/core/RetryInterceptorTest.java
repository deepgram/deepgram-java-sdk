package com.deepgram.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for the {@link RetryInterceptor} class. */
class RetryInterceptorTest {

    private MockWebServer server;
    private OkHttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private OkHttpClient buildClientWithRetries(int maxRetries) {
        return new OkHttpClient.Builder()
                .addInterceptor(new RetryInterceptor(maxRetries))
                .build();
    }

    private OkHttpClient buildClientWithRetriesAndCallTimeout(int maxRetries, long callTimeoutMillis) {
        return new OkHttpClient.Builder()
                .callTimeout(callTimeoutMillis, TimeUnit.MILLISECONDS)
                .addInterceptor(new RetryInterceptor(maxRetries, Optional.of(0L), Optional.of(1000L), Optional.of(0.0)))
                .build();
    }

    private Request buildRequest() {
        return new Request.Builder().url(server.url("/test")).build();
    }

    @Nested
    @DisplayName("Status codes that should trigger retries")
    class RetriableStatusCodes {

        @Test
        @DisplayName("retries on 429 Too Many Requests and eventually succeeds")
        void testRetryOn429() throws Exception {
            client = buildClientWithRetries(2);

            server.enqueue(new MockResponse().setResponseCode(429).setBody("rate limited"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("success"));

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).isEqualTo("success");
            assertThat(server.getRequestCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("retries on 500 Internal Server Error and eventually succeeds")
        void testRetryOn500() throws Exception {
            client = buildClientWithRetries(2);

            server.enqueue(new MockResponse().setResponseCode(500).setBody("server error"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("success"));

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).isEqualTo("success");
            assertThat(server.getRequestCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("retries on 503 Service Unavailable and eventually succeeds")
        void testRetryOn503() throws Exception {
            client = buildClientWithRetries(2);

            server.enqueue(new MockResponse().setResponseCode(503).setBody("unavailable"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("success"));

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).isEqualTo("success");
            assertThat(server.getRequestCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("retries on 408 Request Timeout and eventually succeeds")
        void testRetryOn408() throws Exception {
            client = buildClientWithRetries(2);

            server.enqueue(new MockResponse().setResponseCode(408).setBody("timeout"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("success"));

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).isEqualTo("success");
            assertThat(server.getRequestCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Status codes that should NOT trigger retries")
    class NonRetriableStatusCodes {

        @Test
        @DisplayName("does not retry on 200 OK")
        void testNoRetryOn200() throws Exception {
            client = buildClientWithRetries(2);

            server.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).isEqualTo("ok");
            assertThat(server.getRequestCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("does not retry on 400 Bad Request")
        void testNoRetryOn400() throws Exception {
            client = buildClientWithRetries(2);

            server.enqueue(new MockResponse().setResponseCode(400).setBody("bad request"));

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(400);
            assertThat(response.body().string()).isEqualTo("bad request");
            assertThat(server.getRequestCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("does not retry on 401 Unauthorized")
        void testNoRetryOn401() throws Exception {
            client = buildClientWithRetries(2);

            server.enqueue(new MockResponse().setResponseCode(401).setBody("unauthorized"));

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(401);
            assertThat(response.body().string()).isEqualTo("unauthorized");
            assertThat(server.getRequestCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("does not retry on 403 Forbidden")
        void testNoRetryOn403() throws Exception {
            client = buildClientWithRetries(2);

            server.enqueue(new MockResponse().setResponseCode(403).setBody("forbidden"));

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(403);
            assertThat(response.body().string()).isEqualTo("forbidden");
            assertThat(server.getRequestCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("does not retry on 404 Not Found")
        void testNoRetryOn404() throws Exception {
            client = buildClientWithRetries(2);

            server.enqueue(new MockResponse().setResponseCode(404).setBody("not found"));

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(404);
            assertThat(response.body().string()).isEqualTo("not found");
            assertThat(server.getRequestCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Retry limit behavior")
    class RetryLimits {

        @Test
        @DisplayName("stops retrying after max retries exhausted")
        void testMaxRetriesExhausted() throws Exception {
            client = buildClientWithRetries(2);

            // Enqueue 3 failures (initial + 2 retries) - all should be consumed
            server.enqueue(new MockResponse().setResponseCode(500).setBody("error 1"));
            server.enqueue(new MockResponse().setResponseCode(500).setBody("error 2"));
            server.enqueue(new MockResponse().setResponseCode(500).setBody("error 3"));
            // This 4th response should NOT be reached
            server.enqueue(new MockResponse().setResponseCode(200).setBody("success"));

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(500);
            // 1 initial request + 2 retries = 3 total requests
            assertThat(server.getRequestCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("zero max retries means no retries at all")
        void testZeroRetries() throws Exception {
            client = buildClientWithRetries(0);

            server.enqueue(new MockResponse().setResponseCode(500).setBody("error"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("success"));

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(500);
            assertThat(server.getRequestCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("succeeds on last retry attempt")
        void testSucceedsOnLastRetry() throws Exception {
            client = buildClientWithRetries(3);

            server.enqueue(new MockResponse().setResponseCode(500).setBody("error 1"));
            server.enqueue(new MockResponse().setResponseCode(500).setBody("error 2"));
            server.enqueue(new MockResponse().setResponseCode(500).setBody("error 3"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("success"));

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).isEqualTo("success");
            // 1 initial + 3 retries = 4 total
            assertThat(server.getRequestCount()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Retry-After header handling")
    class RetryAfterHeader {

        @Test
        @DisplayName("respects Retry-After header with seconds value")
        void testRetryAfterSeconds() throws Exception {
            client = buildClientWithRetries(1);

            server.enqueue(new MockResponse()
                    .setResponseCode(429)
                    .setHeader("Retry-After", "1")
                    .setBody("rate limited"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("success"));

            long start = System.currentTimeMillis();
            Response response = client.newCall(buildRequest()).execute();
            long elapsed = System.currentTimeMillis() - start;

            assertThat(response.code()).isEqualTo(200);
            assertThat(server.getRequestCount()).isEqualTo(2);
            // Should have waited at least ~1 second (Retry-After: 1)
            assertThat(elapsed).isGreaterThanOrEqualTo(900L);
        }
    }

    @Nested
    @DisplayName("Mixed status code sequences")
    class MixedSequences {

        @Test
        @DisplayName("retries through multiple different server error codes")
        void testMultipleDifferentErrors() throws Exception {
            client = buildClientWithRetries(3);

            server.enqueue(new MockResponse().setResponseCode(500));
            server.enqueue(new MockResponse().setResponseCode(503));
            server.enqueue(new MockResponse().setResponseCode(429));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("finally"));

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).isEqualTo("finally");
            assertThat(server.getRequestCount()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Retry transport failures")
    class RetryTransportFailures {

        @Test
        @DisplayName("returns the buffered retryable response when a later retry disconnects")
        void preservesEarlierResponseAfterRetryTransportFailure() throws Exception {
            AtomicInteger attempts = new AtomicInteger();
            client = new OkHttpClient.Builder()
                    .addInterceptor(new RetryInterceptor(1, Optional.of(0L), Optional.of(1000L), Optional.of(0.0)))
                    .addInterceptor(chain -> {
                        if (attempts.incrementAndGet() == 1) {
                            return new Response.Builder()
                                    .request(chain.request())
                                    .protocol(Protocol.HTTP_1_1)
                                    .code(500)
                                    .message("original server error")
                                    .header("X-Retry-Source", "original")
                                    .body(ResponseBody.create(
                                            "original server error".getBytes(StandardCharsets.UTF_8),
                                            MediaType.get("text/plain")))
                                    .build();
                        }
                        throw new IOException("retry transport failure");
                    })
                    .build();

            Response response = client.newCall(buildRequest()).execute();

            assertThat(response.code()).isEqualTo(500);
            assertThat(response.header("X-Retry-Source")).isEqualTo("original");
            assertThat(response.body().string()).isEqualTo("original server error");
            assertThat(attempts).hasValue(2);
        }

        @Test
        @DisplayName("does not spend the next attempt timeout budget during Retry-After backoff")
        void pausesCallTimeoutDuringRetryAfterBackoff() throws Exception {
            client = buildClientWithRetriesAndCallTimeout(1, 100);
            server.enqueue(new MockResponse()
                    .setResponseCode(429)
                    .setHeader("Retry-After", "1")
                    .setBody("rate limited"));
            server.enqueue(new MockResponse().setResponseCode(200).setBody("success"));

            long start = System.nanoTime();
            Response response = client.newCall(buildRequest()).execute();
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).isEqualTo("success");
            assertThat(server.getRequestCount()).isEqualTo(2);
            assertThat(elapsedMillis).isGreaterThanOrEqualTo(900L);
        }
    }
}
