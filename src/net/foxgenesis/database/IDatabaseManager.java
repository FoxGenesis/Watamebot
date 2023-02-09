package net.foxgenesis.database;

import java.io.IOException;

public interface IDatabaseManager extends AutoCloseable {
	/**
	 * Check if all guild data has been processed and is ready for use.
	 * 
	 * @return Returns {@code true} when all data has been loaded from the database
	 * @see #isConnectionValid()
	 */
	public boolean isReady();

	boolean register(AbstractDatabase database) throws IOException;

	boolean isDatabaseRegistered(AbstractDatabase database);
	
	String getName();
}
