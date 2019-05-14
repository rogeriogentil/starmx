package config;

import static org.junit.Assert.*;

import org.junit.Test;
import org.starmx.config.ConfigurationException;
import org.starmx.config.ConfigurationLoader;

public class TestConfig {

	@Test
	public void loadConfigFile() {
		try {
			assertNotNull(ConfigurationLoader.loadConfig());
		} catch (ConfigurationException e) {
			e.printStackTrace();

			fail(e.getMessage());
		}
	}
	

}
