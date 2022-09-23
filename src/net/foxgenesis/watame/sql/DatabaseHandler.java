package net.foxgenesis.watame.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.dv8tion.jda.api.entities.Guild;
import net.foxgenesis.config.JSONObjectAdv;

public interface DatabaseHandler {

	/**
	 * Check if the database is connected and is ready
	 * for operations.
	 * @return Returns {@code true} if is connected and ready
	 * @see #registerStatement(String, String)
	 * @see #hasStatement(String)
	 */
	public boolean isValid();
	
	/**
	 * Compile a new {@link PreparedStatement} linked with {@code id} in
	 * the database.
	 * @param id - statement id
	 * @param statement - SQL code to prepare
	 * @return Compiled {@link PreparedStatement} that is ready to be used
	 * @throws UnsupportedOperationException Thrown if id is already registered
	 * or database connection is not valid
	 * @throws SQLException
	 * @see #hasStatement(String)
	 * @see #isValid()
	 */
	public PreparedStatement registerStatement(String id, String statement) throws SQLException;
	
	/**
	 * Returns if the database already has a {@link PreparedStatement}k
	 * with the id {@code id}.
	 * @param id - {@link PreparedStatement} id to check
	 * @return Returns {@code true} if the database has already
	 * registered this id.
	 * @see #registerStatement(String, String)
	 * @see #isValid()
	 */
	public boolean hasStatement(String id);
	
	/**
	 * NEED_JAVADOC
	 * @param g
	 * @return
	 */
	public JSONObjectAdv getDataForGuild(Guild g);
}
