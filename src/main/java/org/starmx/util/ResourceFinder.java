package org.starmx.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.starmx.StarMXProperties;

public class ResourceFinder {

	public static InputStream getResourceAsStream(String name) {
		if (name == null || name.length() == 0)
			return null;

		String configPath = System.getProperty(StarMXProperties.CONFIG_PATH, "");
		if (configPath.length() > 0) {
			if (name.charAt(0) != File.separatorChar
					&& configPath.charAt(configPath.length() - 1) != File.separatorChar) {
				configPath += File.separator;
			}
		}
		
		InputStream is = ResourceFinder.class.getClassLoader().getResourceAsStream(configPath + name);
		if (is == null) {	
			try {
				File f = new File(configPath + name);
				System.out.println(" Loading " + f.getAbsolutePath() + "...");
				return new FileInputStream(f);
				
			} catch (FileNotFoundException e) {
				return null;
			}
		} else {
			return is;
		}
	}
}
