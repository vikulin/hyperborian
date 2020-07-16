package org.hyperborian.bt;

import java.nio.file.Path;
import java.nio.file.Paths;

import lbms.plugins.mldht.DHTConfiguration;
import the8472.utils.ConfigReader;

public class XmlConfig implements DHTConfiguration {
	
	private ConfigReader configReader;

	public XmlConfig(ConfigReader configReader) {
		this.configReader = configReader;
	}
	
	int port;
	boolean multihoming;
	
	public void update() {
		port = configReader.getLong("//core/port").orElse(49001L).intValue();
		multihoming = configReader.getBoolean("//core/multihoming").orElse(true);
	}

	
	@Override
	public boolean noRouterBootstrap() {
		return !configReader.getBoolean("//core/useBootstrapServers").orElse(true);
	}

	@Override
	public boolean isPersistingID() {
		return configReader.getBoolean("//core/persistID").orElse(true);
	}

	@Override
	public Path getStoragePath() {
		return Paths.get(".");
	}

	@Override
	public int getListeningPort() {
		return port;
	}

	@Override
	public boolean allowMultiHoming() {
		return false;
	}
}