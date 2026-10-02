package com.chatflow;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Week3WebSocketDeliveryTests {
    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    private static final String NUL = String.valueOf((char) 0);

    @Test
    void authenticatedStompClientReceivesMessageCreatedEvent() throws Exception {
        String base = "http://127.0.0.1:" + port;
        JsonNode registered = rest.postForObject(base + "/api/v1/auth/register",
                Map.of("username", "wsdelivery_" + System.nanoTime(), "password", "strong-pass-123"), JsonNode.class);
        String token = registered.get("accessToken").asText();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        JsonNode conversation = rest.exchange(base + "/api/v1/conversations", HttpMethod.POST,
                new HttpEntity<>(Map.of("title", "Realtime test"), headers), JsonNode.class).getBody();
        long conversationId = conversation.get("id").asLong();

        BlockingQueue<String> frames = new LinkedBlockingQueue<>();
        StringBuilder fragment = new StringBuilder();
        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override public void onOpen(WebSocket socket) { socket.request(1); }
            @Override public CompletionStage<?> onText(WebSocket socket, CharSequence data, boolean last) {
                fragment.append(data);
                if (last) {
                    frames.offer(fragment.toString());
                    fragment.setLength(0);
                }
                socket.request(1);
                return null;
            }
        };
        WebSocket socket = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
                .newWebSocketBuilder().buildAsync(URI.create("ws://127.0.0.1:" + port + "/ws"), listener)
                .get(10, TimeUnit.SECONDS);
        try {
            socket.sendText("CONNECT\naccept-version:1.2\nhost:localhost\nAuthorization:Bearer " + token + "\n\n" + NUL, true)
                    .get(5, TimeUnit.SECONDS);
            String connected = awaitFrame(frames, "CONNECTED", 5);
            assertThat(connected).contains("CONNECTED");

            socket.sendText("SUBSCRIBE\nid:sub-1\ndestination:/topic/conversations/" + conversationId + "\nack:auto\n\n" + NUL, true)
                    .get(5, TimeUnit.SECONDS);
            Thread.sleep(150);

            rest.exchange(base + "/api/v1/conversations/" + conversationId + "/messages", HttpMethod.POST,
                    new HttpEntity<>(Map.of("content", "hello over websocket", "clientMsgId", "ws-" + System.nanoTime()), headers),
                    JsonNode.class);

            String event = awaitFrame(frames, "message.created", 5);
            assertThat(event).contains("MESSAGE").contains("message.created").contains("hello over websocket");
        } finally {
            try { socket.sendText("DISCONNECT\n\n" + NUL, true).get(2, TimeUnit.SECONDS); } catch (Exception ignored) { }
            socket.abort();
        }
    }

    private String awaitFrame(BlockingQueue<String> frames, String expected, int seconds) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds);
        while (System.nanoTime() < deadline) {
            String frame = frames.poll(200, TimeUnit.MILLISECONDS);
            if (frame != null && frame.contains(expected)) return frame;
        }
        throw new AssertionError("Timed out waiting for STOMP frame containing " + expected + "; queued frames: " + frames);
    }
}
