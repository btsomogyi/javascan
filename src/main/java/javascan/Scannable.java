/**
 * @author Blue Thunder Somogyi
 *
 * Copyright (c) 2016 Blue Thunder Somogyi
 */
package javascan;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

// Interface to allow comingling of Addr and Net objects in single ordered list of results.
public interface Scannable {
	public abstract void output() throws InterruptedException, ExecutionException;
	public abstract void scan() throws UnknownHostException;
}
