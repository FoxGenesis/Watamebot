package net.foxgenesis.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Abstract class defining common methods used to make calls to a JSON web API.
 * 
 * @author Ashley
 */
public abstract class JsonApi implements AutoCloseable {
	/**
	 * Logger
	 */
	protected final Logger logger;

	/**
	 * HTTP client
	 */
	protected final OkHttpClient client;

	/**
	 * Name of the API
	 */
	private final String name;

	/**
	 * Create a new instance with a new HTTP client.
	 * 
	 * @param name - name of this API
	 * 
	 * @see #JsonApi(String, OkHttpClient)
	 */
	public JsonApi(String name) {
		this(name, new OkHttpClient().newBuilder().build());
	}

	/**
	 * Create a new instance using the specified HTTP client.
	 * 
	 * @param client - {@link OkHttpClient} to use for requests
	 * @param name   - name of this API
	 * 
	 * @see #JsonApi(String)
	 */
	@SuppressWarnings("exports")
	public JsonApi(String name, @NotNull OkHttpClient client) {
		this.logger = LoggerFactory.getLogger(getClass());
		this.name = Objects.requireNonNull(name);
		this.client = Objects.requireNonNull(client);
	}

	/**
	 * Send a GET request to the API using the specified {@code end-point}, query
	 * {@code parameters} and request {@code body}. The response will then be read
	 * into the specified java {@code bean}.
	 * 
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 * 
	 * <pre>
	 * request(Method.PUT, endpoint, parameters, body, javaBean)
	 * </pre>
	 * 
	 * @param <T>        - response java bean
	 * @param endpoint   - (optional) end-point of the API
	 * @param parameters - (optional) query parameters of the request
	 * @param body       - (optional) request body
	 * @param javaBean   - class of the java bean to use
	 * 
	 * @return Returns a {@link CompletableFuture} that will complete with the
	 *         specified java {@code bean} or exception.
	 * 
	 * @see #post(String, Map, Supplier, Class)
	 * @see #delete(String, Map, Supplier, Class)
	 * @see #update(String, Map, Supplier, Class)
	 * @see #put(String, Map, Supplier, Class)
	 * @see #request(Method, String, Map, Supplier, Class)
	 */
	@NotNull
	protected <T> CompletableFuture<T> get(@Nullable String endpoint, @Nullable Map<String, String> parameters,
			@Nullable Supplier<RequestBody> body, @NotNull Class<? extends T> javaBean) {
		return request(Method.GET, endpoint, parameters, body, javaBean);
	}

	/**
	 * Send a POST request to the API using the specified {@code end-point}, query
	 * {@code parameters} and request {@code body}. The response will then be read
	 * into the specified java {@code bean}.
	 * 
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 * 
	 * <pre>
	 * request(Method.POST, endpoint, parameters, body, javaBean)
	 * </pre>
	 * 
	 * @param <T>        - response java bean
	 * @param endpoint   - (optional) end-point of the API
	 * @param parameters - (optional) query parameters of the request
	 * @param body       - (optional) request body
	 * @param javaBean   - class of the java bean to use
	 * 
	 * @return Returns a {@link CompletableFuture} that will complete with the
	 *         specified java {@code bean} or exception.
	 * 
	 * @see #get(String, Map, Supplier, Class)
	 * @see #delete(String, Map, Supplier, Class)
	 * @see #update(String, Map, Supplier, Class)
	 * @see #put(String, Map, Supplier, Class)
	 * @see #request(Method, String, Map, Supplier, Class)
	 */
	@NotNull
	protected <T> CompletableFuture<T> post(@Nullable String endpoint, @Nullable Map<String, String> parameters,
			@Nullable Supplier<RequestBody> body, @NotNull Class<? extends T> javaBean) {
		return request(Method.POST, endpoint, parameters, body, javaBean);
	}

	/**
	 * Send a DELETE request to the API using the specified {@code end-point}, query
	 * {@code parameters} and request {@code body}. The response will then be read
	 * into the specified java {@code bean}.
	 * 
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 * 
	 * <pre>
	 * request(Method.DELETE, endpoint, parameters, body, javaBean)
	 * </pre>
	 * 
	 * @param <T>        - response java bean
	 * @param endpoint   - (optional) end-point of the API
	 * @param parameters - (optional) query parameters of the request
	 * @param body       - (optional) request body
	 * @param javaBean   - class of the java bean to use
	 * 
	 * @return Returns a {@link CompletableFuture} that will complete with the
	 *         specified java {@code bean} or exception.
	 * 
	 * @see #get(String, Map, Supplier, Class)
	 * @see #post(String, Map, Supplier, Class)
	 * @see #update(String, Map, Supplier, Class)
	 * @see #put(String, Map, Supplier, Class)
	 * @see #request(Method, String, Map, Supplier, Class)
	 */
	@NotNull
	protected <T> CompletableFuture<T> delete(@Nullable String endpoint, @Nullable Map<String, String> parameters,
			@Nullable Supplier<RequestBody> body, @NotNull Class<? extends T> javaBean) {
		return request(Method.DELETE, endpoint, parameters, body, javaBean);
	}

	/**
	 * Send an UPDATE request to the API using the specified {@code end-point},
	 * query {@code parameters} and request {@code body}. The response will then be
	 * read into the specified java {@code bean}.
	 * 
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 * 
	 * <pre>
	 * request(Method.UPDATE, endpoint, parameters, body, javaBean)
	 * </pre>
	 * 
	 * @param <T>        - response java bean
	 * @param endpoint   - (optional) end-point of the API
	 * @param parameters - (optional) query parameters of the request
	 * @param body       - (optional) request body
	 * @param javaBean   - class of the java bean to use
	 * 
	 * @return Returns a {@link CompletableFuture} that will complete with the
	 *         specified java {@code bean} or exception.
	 * 
	 * @see #get(String, Map, Supplier, Class)
	 * @see #post(String, Map, Supplier, Class)
	 * @see #delete(String, Map, Supplier, Class)
	 * @see #put(String, Map, Supplier, Class)
	 * @see #request(Method, String, Map, Supplier, Class)
	 */
	@NotNull
	protected <T> CompletableFuture<T> update(@Nullable String endpoint, @Nullable Map<String, String> parameters,
			@Nullable Supplier<RequestBody> body, @NotNull Class<? extends T> javaBean) {
		return request(Method.UPDATE, endpoint, parameters, body, javaBean);
	}

	/**
	 * Send a PUT request to the API using the specified {@code end-point}, query
	 * {@code parameters} and request {@code body}. The response will then be read
	 * into the specified java {@code bean}.
	 * 
	 * <p>
	 * This method is effectively equivalent to:
	 * </p>
	 * 
	 * <pre>
	 * request(Method.PUT, endpoint, parameters, body, javaBean)
	 * </pre>
	 * 
	 * @param <T>        - response java bean
	 * @param endpoint   - (optional) end-point of the API
	 * @param parameters - (optional) query parameters of the request
	 * @param body       - (optional) request body
	 * @param javaBean   - class of the java bean to use
	 * 
	 * @return Returns a {@link CompletableFuture} that will complete with the
	 *         specified java {@code bean} or exception.
	 * 
	 * @see #get(String, Map, Supplier, Class)
	 * @see #post(String, Map, Supplier, Class)
	 * @see #update(String, Map, Supplier, Class)
	 * @see #delete(String, Map, Supplier, Class)
	 * @see #request(Method, String, Map, Supplier, Class)
	 */
	@NotNull
	protected <T> CompletableFuture<T> put(@Nullable String endpoint, @Nullable Map<String, String> parameters,
			@Nullable Supplier<RequestBody> body, @NotNull Class<? extends T> javaBean) {
		return request(Method.PUT, endpoint, parameters, body, javaBean);
	}

	/**
	 * Send a request to the API using the specified {@code method},
	 * {@code end-point}, query {@code parameters} and request {@code body}. The
	 * response will then be read into the specified java {@code bean}.
	 * 
	 * @param <T>        - response java bean
	 * @param method     - HTTP method to use
	 * @param endpoint   - (optional) end-point of the API
	 * @param parameters - (optional) query parameters of the request
	 * @param body       - (optional) request body
	 * @param javaBean   - class of the java bean to use
	 * 
	 * @return Returns a {@link CompletableFuture} that will complete with the
	 *         specified java {@code bean} or exception.
	 */
	@NotNull
	protected <T> CompletableFuture<T> request(@NotNull Method method, @Nullable String endpoint,
			@Nullable Map<String, String> parameters, @Nullable Supplier<RequestBody> body,
			@NotNull Class<? extends T> javaBean) {
		return submit(createRequest(method, endpoint, parameters, body))
				.thenApply(response -> readJSONResponse(response, javaBean));
	}

	/**
	 * Get the API token that should be included in all requests.
	 * 
	 * @return Returns the {@link ApiKey} that will be included in all requests
	 */
	@Nullable
	protected abstract ApiKey getApiKey();

	/**
	 * Get the base URL of the API.
	 * 
	 * @return Returns a string containing the URL pointing to the root of the API
	 */
	@NotNull
	protected abstract String getBaseURL();

	/**
	 * Get a map of query parameters to add to every request.
	 * 
	 * @return Returns a {@link Map} containing query parameters to include in all
	 *         requests
	 */
	@Nullable
	protected abstract Map<String, String> getDefaultQueryParameters();

	/**
	 * Merge a query parameter with one that was specified in
	 * {@link #getDefaultQueryParameters()}.
	 * 
	 * @param key          - parameter key
	 * @param specified    - specified parameter value
	 * @param defaultValue - default parameter value
	 * 
	 * @return Returns the merge of {@code specified} and {@code defaultValue}
	 */
	protected String mergeQueryParameter(@NotNull String key, @NotNull String specified,
			@Nullable String defaultValue) {
		return specified + (defaultValue != null ? " " + defaultValue : "");
	}

	/**
	 * Create a new request using the specified method, end-point, query parameters,
	 * and request body.
	 * 
	 * @param method   - HTTP method to use
	 * @param endpoint - (optional) end-point of the API
	 * @param params   - (optional) query parameters
	 * @param body     - (optional) request body
	 * 
	 * @return Returns the created {@link Request}
	 * 
	 * @throws NullPointerException If {@code method} is {@code null} or
	 *                              {@link HttpUrl#parse(String)} returns
	 *                              {@code null}
	 */
	@NotNull
	private Request createRequest(@NotNull Method method, @Nullable String endpoint,
			@Nullable Map<String, String> params, @Nullable Supplier<RequestBody> body) {
		Objects.requireNonNull(method);

		// Add end point if present
		String tmp = getBaseURL();
		if (!(endpoint == null || endpoint.isBlank()))
			tmp += '/' + endpoint;

		// Construct a new request builder
		Builder builder = new Request.Builder();
		Objects.requireNonNull(HttpUrl.parse(tmp)).newBuilder();
		HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(tmp)).newBuilder();

		// Add request body if specified
		if (body != null) {
			// Construct and add body
			RequestBody b = body.get();
			builder.method(method.name().toUpperCase(), b);

			// Add body content type
			MediaType type = b.contentType();
			if (type != null)
				builder.addHeader("Content-Type", type.toString());
		} else
			builder.addHeader("Content-Type", "application/json");

		// Merge specified query parameters with the default ones and add to builder
		Map<String, String> copyMap = params != null ? new HashMap<>(params) : new HashMap<>();
		Map<String, String> defaultMap = getDefaultQueryParameters();
		if (defaultMap != null)
			defaultMap.forEach((key, def) -> copyMap.merge(key, def, (v1, v2) -> mergeQueryParameter(key, v1, v2)));
		copyMap.forEach(httpBuilder::addEncodedQueryParameter);

		// Add API key if present
		ApiKey key = getApiKey();
		if (key != null)
			switch (key.type()) {
				case HEADER -> builder.addHeader(key.name(), key.token());
				case PARAMETER -> httpBuilder.addQueryParameter(key.name(), key.token());
				default -> throw new IllegalArgumentException("Unkown key type: " + key.type());
			}

		// Construct URL
		builder.url(httpBuilder.build());

		// Build the request
		return builder.build();
	}

	/**
	 * Enqueue a {@link Request} and map it's {@link Callback} to a
	 * {@link CompletableFuture}.
	 * 
	 * @param request - request to enqueue
	 * 
	 * @return Returns a {@link CompletableFuture} that will complete normally when
	 *         the {@link Callback#onResponse(Call, Response)} is called. Otherwise
	 *         will complete exceptionally if the
	 *         {@link Callback#onFailure(Call, IOException)} is called.
	 */
	@NotNull
	private CompletableFuture<Response> submit(@NotNull Request request) {
		Objects.requireNonNull(client);
		logger.debug("{} {}", request.method().toUpperCase(), request.url().toString());

		CompletableFuture<Response> callback = new CompletableFuture<>();
		client.newCall(request).enqueue(new FutureCallback(callback));
		return callback;
	}

	/**
	 * Map a {@link Response} to a JavaBean. The specified response's content type
	 * must be {@code application/json}.
	 * 
	 * @param <T>      JavaBean class
	 * @param response - response to get data from
	 * @param javaBean - JavaBean class to map JSON to
	 * 
	 * @return Returns an instance of the {@code javaBean} that was constructed with
	 *         the response's {@link okhttp3.ResponseBody#string() body} content
	 */
	@Nullable
	private <T> T readJSONResponse(@NotNull Response response, @NotNull Class<? extends T> javaBean) {
		// Ensure content type is application/json
		try (ResponseBody body = response.body()) {
			if (body == null)
				throw new NullPointerException("Empty body");

			MediaType type = body.contentType();
			if (type != null && !type.subtype().equals("json"))
				throw new CompletionException(
						new IOException("Returned content type is not application/json: " + type));

			String b = body.string();
			logger.debug(b);

			// Check for empty body
			if (b.equals("[]") || b.equals("{}"))
				return null;

			// Construct the JavaBean from the response body
			ObjectMapper mapper = new ObjectMapper();
			try (JsonParser parser = mapper.createParser(b)) {
				return mapper.readValue(parser, javaBean);
			}
		} catch (IOException e) {
			throw new CompletionException(e);
		}
	}

	/**
	 * Get the executor used for asynchronous calls.
	 * 
	 * @return Returns the {@link Executor} used for asynchronous calls.
	 */
	public Executor getExecutor() {
		return client.dispatcher().executorService();
	}

	/**
	 * Get the name of this API.
	 * 
	 * @return Returns the APIs name
	 */
	public String getName() {
		return name;
	}

	@Override
	public void close() {
		client.dispatcher().executorService().shutdown();
	}

	@Override
	public int hashCode() {
		return Objects.hash(client, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JsonApi other = (JsonApi) obj;
		return Objects.equals(client, other.client) && Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return "JsonApi [" + (name != null ? "name=" + name + ", " : "") + (client != null ? "client=" + client : "")
				+ "]";
	}

	/**
	 * Class that passes {@link Callback#onResponse(Call, Response)} and
	 * {@link Callback#onFailure(Call, IOException)} to the specified
	 * {@link CompletableFuture}.
	 */
	private static class FutureCallback implements Callback {
		private final CompletableFuture<Response> callback;

		public FutureCallback(CompletableFuture<Response> callback) {
			this.callback = Objects.requireNonNull(callback);
		}

		@Override
		public void onFailure(Call arg0, IOException arg1) {
			callback.completeExceptionally(arg1);
		}

		@Override
		public void onResponse(Call arg0, Response arg1) throws IOException {
			callback.complete(arg1);
		}
	}
}
