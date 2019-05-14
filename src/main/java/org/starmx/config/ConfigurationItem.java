package org.starmx.config;

abstract class ConfigurationItem {

	protected Configuration configuration;

	Configuration getConfiguration() {
		return configuration;
	}

	void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
}
