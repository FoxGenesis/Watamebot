package net.foxgenesis.watame.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class AttachmentData {
	private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(
			Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "tiff", "svg", "apng"));
	private static final Set<String> VIDEO_EXTENSIONS = new HashSet<>(
			Arrays.asList("webm", "flv", "vob", "avi", "mov", "wmv", "amv", "mp4", "mpg", "mpeg", "gifv"));

	public final Message message;

	private final Attachment attachment;
	private final URL url;

	private final String fileName;
	private final String extension;

	private final boolean isVideo;
	private final boolean isImage;

	@SuppressWarnings("resource")
	public AttachmentData(Message message, Attachment attachment) {
		this(message, Objects.requireNonNull(attachment), null);
	}

	public AttachmentData(Message message, URL url) {
		this(message, null, Objects.requireNonNull(url));
	}

	private AttachmentData(Message message, Attachment attachment, URL url) {
		this.message = Objects.requireNonNull(message);

		if (attachment == null && url == null)
			throw new IllegalArgumentException("No attachment or URL provided!");

		this.url = url;
		this.attachment = attachment;

		fileName = calculateFileName();
		extension = calculateFileExtension();
		isVideo = this.attachment != null ? this.attachment.isVideo() : VIDEO_EXTENSIONS.contains(getFileExtension());
		isImage = isVideo ? false
				: this.attachment != null ? this.attachment.isImage() : IMAGE_EXTENSIONS.contains(getFileExtension());
	}

	public CompletableFuture<byte[]> getData() {
		return getData(null);
	}

	public CompletableFuture<byte[]> getData(Executor executor) {
		return openAsyncConnection().thenApplyAsync(in -> {
			try (in) {
				return in.readAllBytes();
			} catch (IOException e) {
				throw new CompletionException(e);
			}
		}, executor == null ? ForkJoinPool.commonPool() : executor);
	}

	public String getFileExtension() {
		return extension;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean isVideo() {
		return isVideo;
	}

	public boolean isImage() {
		return isImage;
	}

	public Message getMessage() {
		return message;
	}

	private String calculateFileName() {
		if (attachment != null)
			return attachment.getFileName();

		String p = url.getFile();

		if (p.isBlank())
			return "";

		int l = p.lastIndexOf('/');
		return l == -1 ? "" : p.substring(l);
	}

	private String calculateFileExtension() {
		if (attachment != null)
			return attachment.getFileExtension();

		String p = url.getFile();

		if (p.isBlank())
			return "";

		int l = p.lastIndexOf('.');
		return l == -1 ? "" : p.substring(l + 1);
	}

	@SuppressWarnings("resource")
	public CompletableFuture<InputStream> openAsyncConnection() {
		if (attachment != null)
			return attachment.getProxy().download();

		try {
			return CompletableFuture.completedFuture(url.openStream());
		} catch (IOException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	public InputStream openConnection() throws IOException {
		if (attachment != null)
			return attachment.getProxy().download().join();
		return url.openStream();
	}

	@Override
	public String toString() {
		return "AttachmentData [" + (message != null ? "message=" + message + ", " : "")
				+ (attachment != null ? "attachment=" + attachment + ", " : "")
				+ (url != null ? "url=" + url : "") + "]";
	}
}