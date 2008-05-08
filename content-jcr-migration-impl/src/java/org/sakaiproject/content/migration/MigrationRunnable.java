package org.sakaiproject.content.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MigrationRunnable implements Runnable {
	private static final Log log = LogFactory.getLog(MigrationRunnable.class);
	
	private boolean running = false;
	
	public synchronized void run() {
		while(running) {
			try {
				if(running) {
					// migrate stuff
				}
				else {
					wait();
				}
			} catch (InterruptedException ie) {
				
			}
		}
	}
	
	public synchronized void pause() {
		this.running = false;
	}
	
	public synchronized void resume() {
		this.running = true;
		notify();
	}

}
