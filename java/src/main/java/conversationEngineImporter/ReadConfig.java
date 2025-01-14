package conversationEngineImporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class ReadConfig {
	String fileName;
	String datapackName;
	boolean saveAsZip;

	public ReadConfig() {
		createRun();
		Properties prop = new Properties();
		Properties propBackup = new Properties();

		String propfile = "./config.properties";

		FileInputStream fileIn;
		try {
			try {
				fileIn = new FileInputStream(propfile);
			} catch (FileNotFoundException e) {
				// create a new config file if no cofig file exists.
				createConfigFile();
				fileIn = new FileInputStream(propfile);
				e.printStackTrace();
			}

			prop.load(fileIn);
			propBackup.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fileName = getStringFromConfig("file", prop, propBackup);
		datapackName = getStringFromConfig("name", prop, propBackup);
		saveAsZip = getBoolFromConfig("saveAsZip", prop, propBackup);

	}

	private String getStringFromConfig(String key, Properties prop, Properties propBackup) {

		String a = prop.getProperty(key);
		if (a == null) {
			a = propBackup.getProperty(key);
		}
		if (a == null) {
			System.err.println("unexpected error while loading from config.properties file");
		}

		return a;
	}

	private int getIntFromConfig(String key, Properties prop, Properties propBackup) {
		int a;
		try {
			a = Integer.parseInt(prop.getProperty(key));
		} catch (NumberFormatException e) {
			try {
				a = Integer.parseInt(prop.getProperty(key));
			} catch (NumberFormatException f) {
				a = 0;
				System.err.println("unexpected error while loading from config.properties file");
			}
		}

		return a;
	}

	private boolean getBoolFromConfig(String key, Properties prop, Properties propBackup) {

		boolean a;
		if (prop.getProperty(key) != null && prop.getProperty(key).toLowerCase().equals("true")) {
			a = true;
		} else if (prop.getProperty(key) != null && prop.getProperty(key).toLowerCase().equals("false")) {
			a = false;
		} else {
			if (propBackup.getProperty(key) != null && propBackup.getProperty(key).toLowerCase().equals("true")) {
				a = true;
			} else {
				a = false;
			}
		}

		return a;
	}

	private void createConfigFile() {
		try {
			InputStream source = getClass().getClassLoader().getResourceAsStream("config.properties");
			File target = new File("./config.properties");
			Files.copy(source, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createRun() {
		// check if the file already exist
		File tmpDir = new File("./run.bat");
		if (!tmpDir.exists()) {
			try {
				InputStream source = getClass().getClassLoader().getResourceAsStream("run.bat");
				File target = new File("./run.bat");

				Files.copy(source, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public String getFileName() {
		return fileName;
	}

	public String getDatapackName() {
		return datapackName;
	}

	public boolean isSaveAsZip() {
		return saveAsZip;
	}
}
