package com.deepgram.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.deepgram.core.ReconnectingWebSocketListener.ReconnectOptions;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ReconnectingWebSocketListener} bug fixes. */
class ReconnectingWebSocketListenerTest {

    /**
     * Test fixture: counts how many times the supplier was invoked, and whether each call
     * "succeeded" by simulating a fake WebSocket. Failures are simulated by throwing.
     */
    private static final class CountingSupplier implements Supplier<WebSocket> {
        private final AtomicInteger calls = new AtomicInteger(0);
        private final boolean shouldFail;

        CountingSupplier(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public WebSocket get() {
            calls.incrementAndGet();
            if (shouldFail) {
                throw new RuntimeException("simulated connect failure");
            }
            return new FakeWebSocket();
        }
    }

    private static final class FakeWebSocket implements WebSocket {
        @Override
        public okhttp3.Request request() {
            return new okhttp3.Request.Builder().url("ws://localhost/").build();
        }

        @Override
        public long queueSize() {
            return 0;
        }

        @Override
        public boolean send(String text) {
            return true;
        }

        @Override
        public boolean send(ByteString bytes) {
            return true;
        }

        @Override
        public boolean close(int code, String reason) {
            return true;
        }

        @Override
        public void cancel() {}
    }

    /** Concrete listener that records callback invocations for assertions. */
    private static final class TestListener extends ReconnectingWebSocketListener {
        final AtomicInteger failures = new AtomicInteger(0);

        TestListener(ReconnectOptions options, Supplier<WebSocket> supplier) {
            super(options, supplier);
        }

        @Override
        protected void onWebSocketOpen(WebSocket webSocket, Response response) {}

        @Override
        protected void onWebSocketMessage(WebSocket webSocket, String text) {}

        @Override
        protected void onWebSocketBinaryMessage(WebSocket webSocket, ByteString bytes) {}

        @Override
        protected void onWebSocketFailure(WebSocket webSocket, Throwable t, Response response) {
            failures.incrementAndGet();
        }

        @Override
        protected void onWebSocketClosed(WebSocket webSocket, int code, String reason) {}
    }

    @Nested
    @DisplayName("ReconnectOptions builder")
    class BuilderTests {
        @Test
        @DisplayName("connectionTimeoutMs defaults to 4000")
        void connectionTimeoutDefaultsTo4000() {
            ReconnectOptions opts = ReconnectOptions.builder().build();
            assertThat(opts.connectionTimeoutMs).isEqualTo(4000L);
        }

        @Test
        @DisplayName("connectionTimeoutMs can be customized")
        void connectionTimeoutCustomizable() {
            ReconnectOptions opts =
                    ReconnectOptions.builder().connectionTimeoutMs(15_000L).build();
            assertThat(opts.connectionTimeoutMs).isEqualTo(15_000L);
        }

        @Test
        @DisplayName("connectionTimeoutMs must be positive")
        void connectionTimeoutValidatedPositive() {
            assertThatThrownBy(() -> ReconnectOptions.builder().connectionTimeoutMs(0).build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("connectionTimeoutMs");
            assertThatThrownBy(() -> ReconnectOptions.builder().connectionTimeoutMs(-1).build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("connectionTimeoutMs");
        }

        @Test
        @DisplayName("maxRetries(0) is allowed (no retries, but initial attempt still allowed)")
        void maxRetriesZeroAllowed() {
            ReconnectOptions opts = ReconnectOptions.builder().maxRetries(0).build();
            assertThat(opts.maxRetries).isZero();
        }
    }

    @Nested
    @DisplayName("connect() with maxRetries(0)")
    class MaxRetriesZeroTests {
        @Test
        @DisplayName("allows the initial attempt to proceed (regression: previously refused to connect)")
        void initialAttemptProceedsWhenMaxRetriesIsZero() {
            CountingSupplier supplier = new CountingSupplier(false);
            ReconnectOptions opts = ReconnectOptions.builder().maxRetries(0).build();
            TestListener listener = new TestListener(opts, supplier);

            listener.connect();

            assertThat(supplier.calls.get())
                    .as("initial connect attempt must run even with maxRetries(0)")
                    .isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("applyOptionsOverride")
    class ApplyOverrideTests {
        @Test
        @DisplayName("null override is a no-op")
        void nullOverrideIsNoop() {
            CountingSupplier supplier = new CountingSupplier(false);
            ReconnectOptions opts =
                    ReconnectOptions.builder().maxRetries(7).build();
            TestListener listener = new TestListener(opts, supplier);

            listener.applyOptionsOverride(null);
            listener.connect();

            assertThat(supplier.calls.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("override before connect() applies maxRetries(0) on subsequent retry attempts")
        void overrideAppliesToRetryGate() {
            // Arrange: large initial maxRetries so the listener would normally retry forever.
            CountingSupplier supplier = new CountingSupplier(true /* always fail */);
            ReconnectOptions opts =
                    ReconnectOptions.builder().maxRetries(Integer.MAX_VALUE).build();
            TestListener listener = new TestListener(opts, supplier);

            // Apply the override BEFORE the first connect call. The override survives the connect()
            // call and gates any scheduled retries.
            listener.applyOptionsOverride(
                    ReconnectOptions.builder().maxRetries(0).build());

            listener.connect();
            // The initial attempt still runs (gate is `retryCount > maxRetries`, retryCount=0 → false).
            assertThat(supplier.calls.get()).isEqualTo(1);

            listener.disconnect();
        }
    }
}
