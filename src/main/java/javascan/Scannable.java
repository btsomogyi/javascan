package javascan;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

public interface Scannable {
	public abstract void output() throws InterruptedException, ExecutionException;
	public abstract void scan() throws UnknownHostException;
}
