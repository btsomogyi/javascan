package javascan;

import java.net.UnknownHostException;

public interface Scannable {
	public abstract void output();
	public abstract void scan() throws UnknownHostException;
}
