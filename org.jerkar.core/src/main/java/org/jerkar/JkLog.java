package org.jerkar;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;

import org.jerkar.utils.JkUtilsString;
import org.jerkar.utils.JkUtilsTime;

/**
 * Logger to use to display any relevant information on the running build.
 * 
 * @author Jerome Angibaud
 */
public class JkLog {

	private static final String INDENT = "    ";

	private static final ThreadLocal<LinkedList<Long>> START_TIMES = new ThreadLocal<LinkedList<Long>>();

	private static OffsetStream infoWriter = new OffsetStream(System.out);

	private static OffsetStream errorWriter = new OffsetStream(System.err);

	private static OffsetStream warnWriter = new OffsetStream(System.err);

	/**
	 * Logs a message indicating that a processing has been started. Elipsis are added at the end of the message
	 * and all subsequent logs will be shift right until {@link #done()} is invoked.
	 */
	public static void start(String message) {
		infoWriter.print(message +  " ... " );
		incOffset();
		startTimer();
	}

	public static PrintStream infoStreamIfVerbose() {
		if (JkOptions.isVerbose()) {
			return infoStream();
		}
		return null;
	}

	private static void startTimer() {
		LinkedList<Long> times = START_TIMES.get();
		if (times == null) {
			times = new LinkedList<Long>();
			START_TIMES.set(times);
		}
		times.push(System.nanoTime());
	}

	/**
	 * As {@link #start(String)} but do a carriage return after the start message.
	 */
	public static void startln(String message) {
		start(message);
		nextLine();
	}

	/**
	 * As {@link #startln(String)} but underline the message.
	 */
	public static void startUnderlined(String message) {
		underlined(message);
		incOffset();
		startTimer();
	}

	/**
	 * As {@link #startln(String)} but whith header message.
	 */
	public static void startHeaded(String message) {
		displayHead(message);
		incOffset();
		startTimer();
	}

	public static void trace(String message) {
		if (JkOptions.isVerbose()) {
			JkLog.info(message);
		}
	}


	/**
	 * Notify that the processing notified with 'start' has terminated.
	 * The elapsed time between last {@link #start(String)} invoke and this method invoke is notified.
	 * Also the the shifting due to last 'start' invoke is annihilated.
	 */
	public static void done() {
		doneMessage("Done");
	}

	/**
	 * As {@link #done()} but adding a tailored message.
	 */
	public static void done(String message) {
		doneMessage("Done : " + message);
	}

	private static void doneMessage(String message) {
		decOffset();
		final LinkedList<Long> times = START_TIMES.get();
		if (times == null || times.isEmpty()) {
			throw new IllegalStateException("This 'done' do no match to any 'start'. "
					+"Please, use 'done' only to mention that the previous 'start' activity is done.");
		}
		final long start = times.poll();
		infoWriter.println("==> " + message + " in " + JkUtilsTime.durationInSeconds(start) + " seconds.");

	}

	/**
	 * Displays a message at info level.
	 */
	public static void info(String message) {
		infoWriter.println(message);
	}

	public static void underlined(String message) {
		JkLog.info(message);
		JkLog.info(JkUtilsString.repeat("-", message.length()));
	}

	/**
	 * Displays a multi-line message of the specified message followed by specified lines.
	 */
	public static void info(String message, Iterable<String> lines) {
		infoWriter.print(message);
		for (final String line : lines) {
			infoWriter.println(line);
		}
	}

	/**
	 * Displays multi-line message.
	 */
	public static void info(Iterable<String> lines) {
		for (final String line : lines) {
			infoWriter.println(line);
		}
	}

	/**
	 * Displays multi-line message.
	 */
	public static void info(String ... lines) {
		info(Arrays.asList(lines));
	}

	/**
	 * Displays a multi-line message at warn level.
	 */
	public static void warn(Iterable<String> lines) {
		for (final String line : lines) {
			warn(line);
		}
	}

	/**
	 * Displays a message at warn level.
	 */
	public static void warn(String message) {
		infoWriter.println("WARN : " + message);
	}

	/**
	 * Displays a message at warn level if the specified condition is <code>true</code>.
	 */
	public static void warnIf(boolean condition, String message) {
		if (condition) {
			warn(message);
		}
	}

	/**
	 * Displays a message at warn level.
	 */
	public static void error(String message) {
		errorWriter.println(message);
	}

	public static void error(Iterable<String> lines) {
		for (final String line : lines) {
			errorWriter.println(line);
		}
	}

	public static void nextLine() {
		infoWriter.println();
	}

	public static PrintStream infoStream() {
		return infoWriter;
	}

	public static PrintStream warnStream() {
		return warnWriter;
	}

	public static PrintStream errorStream() {
		return errorWriter;
	}

	private static void decOffset() {
		infoWriter.dec();
		warnWriter.dec();
		errorWriter.dec();
	}

	private static void incOffset() {
		infoWriter.inc();
		warnWriter.inc();
		errorWriter.inc();
	}

	/**
	 * Shifts the left margin. All subsequent log will be shifted <code>delta</code> characters to right.
	 */
	public static void shift(int delta) {
		infoWriter.offsetLevel += delta;
		errorWriter.offsetLevel += delta;
		warnWriter.offsetLevel += delta;
	}

	/**
	 * Returns the current left margin size in character.
	 */
	public static int offset() {
		return infoWriter.offsetLevel;
	}


	private static class OffsetStream extends PrintStream {

		private int offsetLevel;

		public OffsetStream(PrintStream delegate) {
			super(delegate);
		}

		@Override
		public void write(byte[] cbuf, int off, int len)  {
			final byte[] filler = getFiller().getBytes();
			final int lenght = filler.length;
			if (lenght > 0) {
				super.write(filler,0, lenght);
			}
			super.write(cbuf, off, len);
		}

		private String getFiller() {
			if (offsetLevel == 0) {
				return "";
			}
			if (offsetLevel == 1) {
				return INDENT;
			}
			final StringBuilder result = new StringBuilder(INDENT);
			for (int i = 1; i < offsetLevel;i++) {
				result.append(INDENT);
			}
			return result.toString();
		}

		public void inc() {
			offsetLevel++;
		}

		public void dec() {
			if (offsetLevel > 0) {
				offsetLevel--;
			}
		}

	}

	public static void displayHead(String intro) {
		final String pattern = "-";
		JkLog.info(JkUtilsString.repeat(pattern, intro.length() ));
		JkLog.info(intro);
		JkLog.info(JkUtilsString.repeat(pattern, intro.length() ));
	}


}