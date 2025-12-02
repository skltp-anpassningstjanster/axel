package se.inera.axel.test.flapdoodle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import de.flapdoodle.embed.process.extract.TempNaming;

public class FixedTempNaming implements TempNaming {
	
	private String name;
	private String text;
	
	public FixedTempNaming(String text) {
		this.text = text;
	}

	public String nameFor(String prefix, String postfix) {
		name = prefix + "-" + text + "-" + postfix;
		deleteFile();
		return name;
	}
	// Temporary fix. Needs refactoring
	private void deleteFile() {
	    String tempFile = System.getenv("temp") + File.separator + name;

	    try {
			Files.deleteIfExists(new File(tempFile).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}    
	    tempFile = "target/" + File.separator + name;

	    try {
			Files.deleteIfExists(new File(tempFile).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}    
	}
}
