package online.prostobank.clients.utils;

import com.google.common.base.Preconditions;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class NonEmptyInputStream extends FilterInputStream {
	/**
	 * Creates a <code>FilterInputStream</code>
	 * by assigning the  argument <code>in</code>
	 * to the field <code>this.in</code> so as
	 * to remember it for later use.
	 *
	 * @param in the underlying input stream, or <code>null</code> if
	 *           this instance is to be created without an underlying stream.
	 */
	public NonEmptyInputStream(InputStream in) throws IOException, EmptyInputStreamException {
		super( checkStreamIsNotEmpty(in) );
	}

	private static InputStream checkStreamIsNotEmpty(InputStream inputStream) throws IOException, EmptyInputStreamException {
		Preconditions.checkArgument(inputStream != null,"The InputStream is mandatory");
		PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream);
		int b;
		b = pushbackInputStream.read();
		if ( b == -1 ) {
			throw new EmptyInputStreamException("No byte can be read from stream " + inputStream);
		}
		pushbackInputStream.unread(b);
		return pushbackInputStream;
	}

	public static class EmptyInputStreamException extends RuntimeException {
		public EmptyInputStreamException(String message) {
			super(message);
		}
	}
}
