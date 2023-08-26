package net.foxgenesis.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PushBullet {

	private static final Logger logger = LoggerFactory.getLogger(PushBullet.class);

	private OkHttpClient client;
	private final String token;

	public PushBullet(String token) {
		this(new OkHttpClient().newBuilder().build(), token);
	}

	public PushBullet(@SuppressWarnings("exports") @NotNull OkHttpClient client, String token) {
		this.client = Objects.requireNonNull(client);
		this.token = token;
	}

	public void pushPBMessage(String title, String message) {
		if (token != null) {
			try {
				HashMap<String, String> param = new HashMap<String, String>();
				param.put("type", "note");
				param.put("title", title);
				param.put("body", message);
				submit(client, newRequest(token, param)).thenAccept(r -> r.close()).join();
			} catch (Exception e) {
				logger.error("Error while pushing to PushBullet", e);
			}
		}
	}

	private static Request newRequest(String token, Map<String, String> params) throws JsonProcessingException {
		// Construct a new request builder
		Builder builder = new Request.Builder();
		builder.url("https://api.pushbullet.com/v2/pushes");

		// Add request body if specified
		ObjectMapper mapper = new ObjectMapper();
		RequestBody b = RequestBody.create(mapper.writeValueAsBytes(params));
		builder.addHeader("Content-Type", "application/json");
		builder.post(b);
		builder.addHeader("Access-Token", token);

		// Build the request
		return builder.build();
	}

	/**
	 * Enqueue a {@link Request} to an {@link OkHttpClient} and map it's
	 * {@link Callback} to a {@link CompletableFuture}.
	 * 
	 * @param client  - HTTP client to use
	 * @param request - request to enqueue
	 * 
	 * @return Returns a {@link CompletableFuture} that will complete normally when
	 *         the {@link Callback#onResponse(Call, Response)} is called. Otherwise
	 *         will complete exceptionally if the
	 *         {@link Callback#onFailure(Call, IOException)} is called.
	 */
	private static CompletableFuture<Response> submit(OkHttpClient client, Request request) {
		Objects.requireNonNull(client);
		CompletableFuture<Response> callback = new CompletableFuture<>();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call arg0, IOException arg1) {
				callback.completeExceptionally(arg1);
			}

			@Override
			public void onResponse(Call arg0, Response arg1) throws IOException {
				callback.complete(arg1);
			}
		});
		return callback;
	}
}
