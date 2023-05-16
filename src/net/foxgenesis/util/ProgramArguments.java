package net.foxgenesis.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to parse command line parameters.
 * <p>
 * Parsing code taken from <a>https://stackoverflow.com/a/7341724</a>
 * </P>
 * 
 * @author Ashley
 * 
 * @deprecated
 *
 */
@Deprecated(forRemoval = true)
public class ProgramArguments {

	/**
	 * NEED_JAVADOC
	 */
	private final List<String> argsList = new ArrayList<>();

	/**
	 * NEED_JAVADOC
	 */
	private final HashMap<String, String> parameterList = new HashMap<>();

	/**
	 * NEED_JAVADOC
	 */
	private final List<String> flagList = new ArrayList<String>();

	/**
	 * NEED_JAVADOC
	 * 
	 * @see #ProgramArguments(String[])
	 */
	public ProgramArguments() {}

	/**
	 * NEED_JAVADOC
	 * 
	 * @param args - program arguments to parse
	 * 
	 * @see #ProgramArguments()
	 */
	public ProgramArguments(String[] args) {
		parse(args);
	}

	/**
	 * Parse the given arguments into {@code flags}, {@code parameters} and
	 * {@code arguments}.
	 * 
	 * @param args - program arguments to parse
	 */
	public void parse(String[] args) {
		for (int i = 0; i < args.length; i++) {

			switch (args[i].charAt(0)) {

				case '-':
					if (args[i].length() < 2)
						throw new IllegalArgumentException("Not a valid argument: " + args[i]);

					switch (args[i].charAt(1)) {
						case '-':
							if (args[i].length() < 3)
								throw new IllegalArgumentException("Not a valid argument: " + args[i]);
							// --opt
							flagList.add(args[i].substring(2));
							break;
						case 'D':
							// System property
							continue;
						default:
							if (args.length - 1 == i)
								throw new IllegalArgumentException("Expected arg after: " + args[i]);
							// -opt
							parameterList.put(args[i].substring(1), args[i + 1]);
							i++;
					}
					break;
				// if (args[i].charAt(1) == '-') {
				// if (args[i].length() < 3)
				// throw new IllegalArgumentException("Not a valid argument: " + args[i]);
				// // --opt
				// flagList.add(args[i].substring(2));
				// } else {
				// if (args.length - 1 == i)
				// throw new IllegalArgumentException("Expected arg after: " + args[i]);
				// // -opt
				// parameterList.put(args[i].substring(1), args[i + 1]);
				// i++;
				// }

				default:
					// arg
					argsList.add(args[i]);
					break;
			}

		}
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @return
	 */
	public int argumentCount() {
		return argsList.size();
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @return
	 */
	public int flagCount() {
		return flagList.size();
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @return
	 */
	public int parameterCount() {
		return parameterList.size();
	}

	/**
	 * Check if the {@code program arguments} contains the flag "{@code --key}".
	 * 
	 * @param key - flag to check
	 * 
	 * @return returns {@code true} if the arguments contains the flag
	 */
	public boolean hasFlag(String key) {
		return flagList.contains(key);
	}

	/**
	 * Check if the {@code program arguments} contains the parameter "{@code -key}".
	 * 
	 * @param key - parameter to check
	 * 
	 * @return returns {@code true} if the arguments contains the parameter
	 */
	public boolean hasParameter(String key) {
		return parameterList.containsKey(key);
	}

	/**
	 * Get the value of parameter {@code key}.
	 * 
	 * @param key - The parameter key
	 * 
	 * @return Returns the {@code value} of parameter {@code key}.
	 * 
	 * @throws NullPointerException Thrown if parameter list does not contain the
	 *                              {@code key}.
	 * 
	 * @see #hasParameter(String)
	 */
	public String getParameter(String key) {
		if (hasParameter(key))
			return parameterList.get(key);
		throw new NullPointerException("Parameter list does not contain key \"" + key + "\"");
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @return
	 */
	public Map<String, String> getParameters() {
		return Collections.unmodifiableMap(parameterList);
	}

	/**
	 * NEED_JAVADOC
	 * 
	 * @return
	 */
	public List<String> getArguments() {
		return List.copyOf(argsList);
	}

	/**
	 * NEED_JAVADOC
	 */
	public void clear() {
		argsList.clear();
		parameterList.clear();
		flagList.clear();
	}

	/**
	 * NEED_JAVADOC
	 */
	@Override
	public String toString() {
		// ProgramArguments{flagList=[...], parameterList={...}, argsList=[...]}
		StringBuilder builder = new StringBuilder("ProgramArguments{flagList=");

		builder.append(Arrays.toString(flagList.toArray(new String[] {})));
		builder.append(", parameterList=");

		builder.append(parameterList);
		builder.append(", argsList=");

		builder.append(Arrays.toString(argsList.toArray(new String[] {})));
		builder.append('}');

		return builder.toString();
	}
}
