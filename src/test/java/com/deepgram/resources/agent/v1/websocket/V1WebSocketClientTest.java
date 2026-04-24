package com.deepgram.resources.agent.v1.websocket;

import static org.assertj.core.api.Assertions.assertThat;

import com.deepgram.core.ClientOptions;
import com.deepgram.core.Environment;
import com.deepgram.resources.agent.v1.types.AgentV1History;
import com.deepgram.resources.agent.v1.types.AgentV1HistoryContent;
import com.deepgram.resources.agent.v1.types.AgentV1HistoryContentRole;
import com.deepgram.resources.agent.v1.types.AgentV1HistoryFunctionCalls;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class V1WebSocketClientTest {

    @Test
    @DisplayName("dispatches History conversation text messages to the typed history handler")
    void dispatchesHistoryConversationMessages() throws Exception {
        V1WebSocketClient client = createClient();
        AtomicReference<AgentV1History> history = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();
        client.onAgentV1History(history::set);
        client.onError(error::set);

        invokeHandleIncomingMessage(client, "{\"type\":\"History\",\"role\":\"user\",\"content\":\"hello\"}");

        assertThat(error.get()).isNull();
        assertThat(history.get()).isNotNull();

        String variant = history.get().visit(new AgentV1History.Visitor<String>() {
            @Override
            public String visit(AgentV1HistoryContent value) {
                assertThat(value.getType()).isEqualTo("History");
                assertThat(value.getRole()).isEqualTo(AgentV1HistoryContentRole.USER);
                assertThat(value.getContent()).isEqualTo("hello");
                return "content";
            }

            @Override
            public String visit(AgentV1HistoryFunctionCalls value) {
                return "function_calls";
            }
        });

        assertThat(variant).isEqualTo("content");
    }

    @Test
    @DisplayName("dispatches History function call messages to the typed history handler")
    void dispatchesHistoryFunctionCallMessages() throws Exception {
        V1WebSocketClient client = createClient();
        AtomicReference<AgentV1History> history = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();
        client.onAgentV1History(history::set);
        client.onError(error::set);

        invokeHandleIncomingMessage(
                client,
                "{" +
                "\"type\":\"History\"," +
                "\"function_calls\":[{" +
                "\"id\":\"fc_123\"," +
                "\"name\":\"lookup_weather\"," +
                "\"client_side\":true," +
                "\"arguments\":\"{\\\"city\\\":\\\"London\\\"}\"," +
                "\"response\":\"sunny\"" +
                "}]}" );

        assertThat(error.get()).isNull();
        assertThat(history.get()).isNotNull();

        String variant = history.get().visit(new AgentV1History.Visitor<String>() {
            @Override
            public String visit(AgentV1HistoryContent value) {
                return "content";
            }

            @Override
            public String visit(AgentV1HistoryFunctionCalls value) {
                assertThat(value.getType()).isEqualTo("History");
                assertThat(value.getFunctionCalls()).hasSize(1);
                assertThat(value.getFunctionCalls().get(0).getId()).isEqualTo("fc_123");
                assertThat(value.getFunctionCalls().get(0).getName()).isEqualTo("lookup_weather");
                assertThat(value.getFunctionCalls().get(0).getClientSide()).isTrue();
                assertThat(value.getFunctionCalls().get(0).getArguments()).isEqualTo("{\"city\":\"London\"}");
                assertThat(value.getFunctionCalls().get(0).getResponse()).isEqualTo("sunny");
                return "function_calls";
            }
        });

        assertThat(variant).isEqualTo("function_calls");
    }

    private static V1WebSocketClient createClient() {
        ClientOptions clientOptions =
                ClientOptions.builder().environment(Environment.PRODUCTION).build();
        return new V1WebSocketClient(clientOptions);
    }

    private static void invokeHandleIncomingMessage(V1WebSocketClient client, String json) throws Exception {
        Method handleIncomingMessage = V1WebSocketClient.class.getDeclaredMethod("handleIncomingMessage", String.class);
        handleIncomingMessage.setAccessible(true);
        handleIncomingMessage.invoke(client, json);
    }
}
