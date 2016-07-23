package io.ologn.gitstat;

import io.ologn.gitstat.config.ConfigRunner;

public class Main {

	public static void main(String[] args) {
		long time0 = System.currentTimeMillis();
		ConfigRunner.run(args);
		long time1 = System.currentTimeMillis();
		System.out.println("\nElapsed time: " + (time1 - time0) + " millis");
		System.exit(0);
	}

}
