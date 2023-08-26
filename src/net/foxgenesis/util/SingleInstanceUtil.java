package net.foxgenesis.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;

/**
 * Utility class that tries to create a Lock on a specific PID file. If the
 * locking fails, we know that another instance of the application is running.
 *
 * @author Spaz-Master, Ashley
 *
 */
public final class SingleInstanceUtil {

	/**
	 * PID file locking class to use for single instance checks
	 */
	private static PIDLock lock;

	/**
	 * Attempt to obtain lock on PID with {@code amt} retries.
	 * <p>
	 * This method is equivalent to <blockquote>
	 *
	 * <pre>
	 * SingleInstanceUtil.waitAndGetLock(8007, amt, 10_000)
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * @param amt - Amount of retries before failing to obtain lock
	 * 
	 * @throws SingleInstanceLockException Thrown if try count equals or exceeds
	 *                                     {@code amt}
	 * 
	 * @see #waitAndGetLock(File, int)
	 */
	public static void waitAndGetLock(int amt) {
		waitAndGetLock(new File(".pid"), amt, 10_000);
	}

	/**
	 * Attempt to obtain lock on {@code PID} with {@code amt} retries and 10 second
	 * delay between retries.
	 * <p>
	 * This method is equivalent to <blockquote>
	 *
	 * <pre>
	 * SingleInstanceUtil.waitAndGetLock(8007, amt, 10_000)
	 * </pre>
	 *
	 * </blockquote>
	 *
	 * @param file - location of PID file should attempt to lock
	 * @param amt  - Amount of retries before failing to obtain lock
	 * 
	 * @throws SingleInstanceLockException Thrown if try count equals or exceeds
	 *                                     {@code amt}
	 * 
	 * @see #waitAndGetLock(File, int, int)
	 */
	public static void waitAndGetLock(File file, int amt) {
		waitAndGetLock(file, amt, 10_000);
	}

	/**
	 * Attempt to obtain lock on PID file {@code pid}, {@code amt} times with
	 * {@code delay} delay between retries.
	 *
	 * @param file  - location of PID file
	 * @param amt   - Amount of retries before failing to obtain lock
	 * @param delay - Delay between retries
	 * 
	 * @throws SingleInstanceLockException Thrown if try count equals or exceeds
	 *                                     {@code amt}
	 * 
	 * @see #waitAndGetLock(File, int)
	 */
	public static void waitAndGetLock(File file, int amt, int delay) {
		// Check if this program has already obtained lock
		if (lock != null && lock.isValid())
			throw new UnsupportedOperationException("Single instance lock already obtained!");

		@SuppressWarnings("resource") // Closed on shutdown
		PIDLock tempLock = new PIDLock(file);
		int tries = 0;

		do {
			// This is our first try
			tries++;

			try {
				// Attempt to lock on PID
				tempLock.tryLock();
				break;
			} catch (IOException ex) {
				// If try count exceeds or equals retry then throw error
				if (tries >= amt)
					throw new SingleInstanceLockException(file, amt);

				// Wait 'delay' seconds before retrying
				System.err.println("Unable to obtain lock. Retrying again in 5 seconds.");
				ex.printStackTrace();
			}

			// Wait for next retry
			try {
				Thread.sleep(Math.abs(delay));
			} catch (InterruptedException e2) {}

		} while (true); // Keep going until we break or error is thrown

		// Assign our PID lock
		lock = tempLock;

		// Create shutdown thread to close PID lock on program exit
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				lock.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, "PIDLock release thread"));
	}

	/**
	 * Class that obtains and holds a PID lock on a file.
	 *
	 * @author Ashley
	 *
	 */
	private static class PIDLock implements AutoCloseable {
		/**
		 * PID lock file
		 */
		private final File file;

		/**
		 * File channel that holds our lock
		 */
		private FileChannel channel;

		/**
		 * Create a new instance with a specific file to lock to.
		 *
		 * @param file - {@link File} to use as a PID lock
		 */
		public PIDLock(File file) {
			// Ensure file is non null
			this.file = Objects.requireNonNull(file);

			// Ensure our file exists and is deleted on exit
			ensureFile();
		}

		/**
		 * Attempt to obtain an exclusive lock on the file.
		 *
		 * @throws IOException Thrown if a lock could not be obtained or other
		 *                     operations were unable to be performed
		 */
		@SuppressWarnings("resource")
		public void tryLock() throws IOException {
			// Open an output stream to our file
			@SuppressWarnings("resource") // Stream will be closed by channel close method
			FileOutputStream out = new FileOutputStream(file);

			/// Get the stream channel
			FileChannel tempChannel = out.getChannel();

			// attempt to obtain lock and write current process id
			tempChannel.lock();
			tempChannel.write(ByteBuffer.wrap(("" + ProcessHandle.current().pid()).getBytes()));

			// Lock and write complete. set current channel
			channel = tempChannel;
		}

		/**
		 * Ensure file is created and set for deletion upon program exit.
		 */
		private void ensureFile() {
			// Create new file if one doesn't exist
			if (!file.exists())
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}

			// Ensure file is deleted upon system exit
			file.deleteOnExit();
		}

		/**
		 * Check if the lock is valid.
		 *
		 * @return Returns {@code true} if the PID file channel is not {@code null} and
		 *         is open
		 */
		public boolean isValid() {
			return channel != null && channel.isOpen();
		}

		@Override
		public void close() throws IOException {
			channel.close();
		}
	}

	/**
	 * Exception class thrown when SingleInstanceUtil fails to obtain a lock on its
	 * desired port.
	 *
	 * @author Ashley
	 *
	 */
	public static class SingleInstanceLockException extends RuntimeException {

		/**
		 * serial id for serialization
		 */
		private static final long serialVersionUID = -3575228601357312336L;

		/**
		 * location of PID file
		 */
		private final File pidLocation;

		/**
		 * Amount of retries used
		 */
		private final int retries;

		public SingleInstanceLockException(File file, int retries) {
			// Call parent constructor
			pidLocation = file;
			this.retries = retries;
		}

		@Override
		public String getMessage() {
			return String.format("Failed to obtain lock on PID file %s after %d retries.", pidLocation, retries);
		}
	}
}
