package net.foxgenesis.util;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;



/**
 * Utility class that tries to create a Lock on a specific
 * PID file. If the locking fails, we know that another instance
 * of the application is running. 
 * @author Spaz-Master
 *
 */
public final class SingleInstanceUtil {
	/**
	 * PID lock instance
	 */
	private static FileLock lock = null;
        /**
         * PID File channel instance
         */
        private static FileChannel fc = null;
        /**
         * PID file output stream instance
         */
        private static FileOutputStream fos = null;
        /**
         * File object of PID file
         */
        private static File f = null;
        /**
         * Location of PID lock
         */
        private static final String PID_LOCATION = 
                (System.getProperty("file.separator").charAt(0) == '/' ? "/tmp/Watamebot.pid" : System.getenv("TEMP") + "\\Watamebot.pid");
	
        
        
        
        
	/**
	 * Attempt to obtain lock on
	 * PID with {@code amt} retries.
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
		waitAndGetLock(PID_LOCATION, amt, 10_000);
	}
	
	/**
	 * Attempt to obtain lock on
	 * {@code PID} with {@code amt} retries and 
	 * 10 second delay between retries.
	 * <p>
	 * This method is equivalent to
	 * <blockquote><pre>
	 * SingleInstanceUtil.waitAndGetLock(8007, amt, 10_000)
	 * </pre></blockquote>
	 * @param pid - location of PID file should attempt to lock
	 * @param amt - Amount of retries before failing to obtain lock
	 * @throws SingleInstanceLockException Thrown if try count equals or exceeds {@code amt}
	 * @see #waitAndGetLock(int, int, int)
	 */
	public static void waitAndGetLock(String pid, int amt) {
		waitAndGetLock(pid, amt, 10_000);
	}
	
	/**
	 * Attempt to obtain lock on PID file
	 * {@code pid}, {@code amt} times with
	 * {@code delay} delay between retries.
         * @param pid - location of PID file
	 * @param amt - Amount of retries before failing to obtain lock
	 * @param delay - Delay between retries
	 * @throws SingleInstanceLockException Thrown if try count equals or exceeds {@code amt}
	 * @see #waitAndGetLock(int)
	 */
	public static void waitAndGetLock(String pid, int amt, int delay) {
		int tries = 0;
		
		do {
			// This is our first try
			tries++;
			
                        
                                
                    try {
                        // Attempt to lock on PID
                        f = new File(pid);
                        f.createNewFile();
                        fos = new FileOutputStream(f);
                        fc = fos.getChannel();
                        lock = fc.tryLock();
                        // If lock isn't null and is locked, break out of the loop
			if(lock != null){
                            long l = ProcessHandle.current().pid();
                            fos.write(String.format("%d", l).getBytes() );
                            break;
                        }
                        
                    } catch (IOException ex) {
                        lock = null;
                    }
				
			
			// If try count exceeds or equals retry then throw error
			if(tries >= amt)
                            throw new SingleInstanceLockException(pid, amt);
				
			// Wait 'delay' seconds before retrying
			System.err.println("Unable to obtain lock. Retrying again in 5 seconds.");
                    try {Thread.sleep(Math.abs(delay));} catch (InterruptedException e2) {}
			
			
		} while(true); // Keep going until we break or error is thrown

		// Create shutdown thread to close PID lock on program exit
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			// Redundant check if our PID lock exists and is bound
			try {
                            if(lock != null)
                                lock.close();
                            if(fc != null)
                                fc.close();
                            if(fos != null)
                                fos.close();
                            if(f != null)
                                f.delete();
                            
                        } catch (IOException e) {
                            e.printStackTrace();
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
                 * location of PID file
                 */
                private final String pidLocation;
		
		/**
		 * Amount of retries used
		 */
		private final int retries;
		
		public SingleInstanceLockException(String pid, int retries) {
			// Call parent constructor
			super();
                        this.pidLocation = pid;
			this.retries = retries;
		}
		
		@Override
		public String getMessage() {
			return String.format("Failed to obtain lock on PID file %s after %d retries.", pidLocation, retries);
		}
	}
}
