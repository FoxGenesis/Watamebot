package net.foxgenesis.util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Utility class that tries to create a socket on a specific
 * port. If the port binding fails, we know that another instance
 * of the application is running. 
 * @author Ashley
 *
 */
public final class SingleInstanceUtil {
	/**
	 * Socket instance
	 */
	private static ServerSocket socket;
	
	/**
	 * Attempt to obtain socket binding on
	 * port 8007 with {@code amt} retries.
	 * <p>
	 * This method is equivalent to
	 * <blockquote><pre>
	 * SingleInstanceUtil.waitAndGetLock(8007, amt, 10_000)
	 * </pre></blockquote>
	 * @param amt - Amount of retries before failing to obtain lock
	 * @throws SingleInstanceLockException Thrown if try count equals or exceeds {@code amt}
	 * @see #waitAndGetLock(int, int)
	 */
	public static void waitAndGetLock(int amt) {
		waitAndGetLock(8007, amt, 10_000);
	}
	
	/**
	 * Attempt to obtain socket binding on
	 * {@code port} with {@code amt} retries and 
	 * 10 second delay between retries.
	 * <p>
	 * This method is equivalent to
	 * <blockquote><pre>
	 * SingleInstanceUtil.waitAndGetLock(8007, amt, 10_000)
	 * </pre></blockquote>
	 * @param port - Port that the socket should attempt to bind to
	 * @param amt - Amount of retries before failing to obtain lock
	 * @throws SingleInstanceLockException Thrown if try count equals or exceeds {@code amt}
	 * @see #waitAndGetLock(int, int, int)
	 */
	public static void waitAndGetLock(int port, int amt) {
		waitAndGetLock(port, amt, 10_000);
	}
	
	/**
	 * Attempt to obtain socket binding on port
	 * {@code port}, {@code amt} times with
	 * {@code delay} delay between retries.
	 * @param port - Port that the socket should attempt to bind to
	 * @param amt - Amount of retries before failing to obtain lock
	 * @param delay - Delay between retries
	 * @throws SingleInstanceLockException Thrown if try count equals or exceeds {@code amt}
	 * @see #waitAndGetLock(int)
	 */
	public static void waitAndGetLock(int port, int amt, int delay) {
		int tries = 0;
		
		do {
			// This is our first try
			tries++;
			
			try {
				// Attempt to bind on port
				socket = new ServerSocket(port);
				
				// If socket isn't null and is bound, break out of the loop
				if(socket != null && socket.isBound())
					break;
				
			} catch (IOException e) { // Failed to bind to the socket
				
				// If try count exceeds or equals retry then throw error
				if(tries >= amt)
					throw new SingleInstanceLockException(port, amt);
				
				// Wait 'delay' seconds before retrying
				System.err.println("Unable to obtain lock. Retrying again in 5 seconds.");
				try {Thread.sleep(Math.abs(delay));} catch (InterruptedException e2) {}
			}
			
		} while(true); // Keep going until we break or error is thrown

		// Create shutdown thread to close socket on program exit
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// Redundant check if our socket exists and is bound
			if(socket != null && socket.isBound()) {
				try {
					// Attempt to close the socket
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, "SingleInstanceUtil shutdown thread"));
	}
	
	/**
	 * Exception class thrown when SingleInstanceUtil fails to obtain
	 * a lock on its desired port
	 * @author Ashley
	 *
	 */
	public static class SingleInstanceLockException extends RuntimeException {

		/**
		 * serial id for serialization
		 */
		private static final long serialVersionUID = -3575228601357312336L;
		
		/**
		 * Port that was attempted to bind to
		 */
		private final int port;
		
		/**
		 * Amount of retries used
		 */
		private final int retries;
		
		public SingleInstanceLockException(int port, int retries) {
			// Call parent constructor
			super();
			
			this.port = port;
			this.retries = retries;
		}
		
		@Override
		public String getMessage() {
			return String.format("Failed to obtain lock on port %1$d after %2$d retries.", port, retries);
		}
	}
}
