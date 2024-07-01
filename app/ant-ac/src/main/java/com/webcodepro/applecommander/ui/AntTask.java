/*
 * ac - an AppleCommander command line utility
 * Copyright (C) 2002-2022 by Robert Greene
 * robgreene at users.sourceforge.net
 * Copyright (C) 2003-2022 by John B. Matthews
 * matthewsj at users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.webcodepro.applecommander.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.webcodepro.applecommander.storage.Disk;
import com.webcodepro.applecommander.storage.DiskException;
import com.webcodepro.applecommander.storage.FormattedDisk;

/*
 * Copyright (C) 2012 by David Schmidt
 * david__schmidt at users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */


/*
	Fixing code Smells 2024 Yingzhe Xu
		Refactored Long method execute()
 */
public class AntTask extends Task {

	private boolean failOnError = true;
	private String input = null;
	private String output = null;
	private String command = null;
	private String imageName = null;
	private String fileName = null;
	private String volumeName = "ACDISK";
	private String outputPath = null;
	private String type = null;
	private String address = "0x2000";
	private String sizeBlocks = "0";

	public void execute() throws BuildException {
		try {
			switch (command) {
				case "i":
					handleInfo();
					break;
				case "e":
				case "g":
					handleFileExtraction();
					break;
				case "p":
				case "cc65":
				case "as":
					handleFilePut();
					break;
				case "d":
					handleFileDeletion();
					break;
				case "n":
					handleSetDiskName();
					break;
				case "k":
				case "u":
					handleFileLocking();
					break;
				case "ls":
				case "l":
				case "ll":
					handleDirectoryListing();
					break;
				case "dos140":
					createDosDisk(Disk.APPLE_140KB_DISK);
					break;
				case "pro800":
				case "pro140":
					createProDisk(command.equals("pro800") ? Disk.APPLE_800KB_DISK : Disk.APPLE_140KB_DISK);
					break;
				case "pas800":
				case "pas140":
					createPasDisk(command.equals("pas800") ? Disk.APPLE_800KB_DISK : Disk.APPLE_140KB_DISK);
					break;
				case "x":
					handleFileExtractionToPath();
					break;
				case "convert":
					handleConvert();
					break;
				default:
					throw new BuildException("Command \"" + command + "\" not implemented.");
			}
		} catch (Exception ex) {
			handleError(ex);
		}
	}

	private void handleInfo() throws Exception {
		String[] onlyOneImage = { "nonsense", imageName };
		com.webcodepro.applecommander.ui.ac.getDiskInfo(onlyOneImage);
	}

	private void handleFileExtraction() throws Exception {
		try (PrintStream outfile = output != null ? new PrintStream(new FileOutputStream(output)) : System.out) {
			com.webcodepro.applecommander.ui.ac.getFile(imageName, fileName, command.equals("e"), outfile);
		}
	}

	private void handleFilePut() throws Exception {
		if (command.equals("p")) {
			com.webcodepro.applecommander.ui.ac.putFile(input, imageName, fileName, type, address);
		} else if (command.equals("cc65") || command.equals("as")) {
			com.webcodepro.applecommander.ui.ac.putDOS(input, imageName, fileName, type);
			if (command.equals("cc65")) {
				System.err.println("Note: 'cc65' is deprecated.  Please use 'as' or 'dos' as appropriate.");
			}
		} else {
			com.webcodepro.applecommander.ui.ac.putAppleSingle(imageName, fileName, new FileInputStream(input));
		}
	}

	private void handleFileDeletion() throws IOException, DiskException {
		com.webcodepro.applecommander.ui.ac.deleteFile(imageName, fileName);
	}

	private void handleSetDiskName() throws IOException, DiskException {
		com.webcodepro.applecommander.ui.ac.setDiskName(imageName, volumeName);
	}

	private void handleFileLocking() throws IOException, DiskException {
		com.webcodepro.applecommander.ui.ac.setFileLocked(imageName, fileName, command.equals("k"));
	}

	private void handleDirectoryListing() throws IOException {
		String[] onlyOneImage = { "nonsense", imageName };
		int displayType = command.equals("ls") ? FormattedDisk.FILE_DISPLAY_STANDARD :
				command.equals("l") ? FormattedDisk.FILE_DISPLAY_NATIVE :
						FormattedDisk.FILE_DISPLAY_DETAIL;
		com.webcodepro.applecommander.ui.ac.showDirectory(DirectoryLister.text(displayType), onlyOneImage);
	}

	private void createDosDisk(int diskType) throws IOException {
		com.webcodepro.applecommander.ui.ac.createDosDisk(imageName, diskType);
	}

	private void createProDisk(int diskType) throws IOException {
		com.webcodepro.applecommander.ui.ac.createProDisk(imageName, volumeName, diskType);
	}

	private void createPasDisk(int diskType) throws IOException {
		com.webcodepro.applecommander.ui.ac.createPasDisk(imageName, volumeName, diskType);
	}

	private void handleFileExtractionToPath() throws IOException, DiskException {
		com.webcodepro.applecommander.ui.ac.getFiles(imageName, outputPath);
	}

	private void handleConvert() throws IOException {
		com.webcodepro.applecommander.ui.ac.convert(fileName, imageName, Integer.parseInt(sizeBlocks));
	}

	private void handleError(Exception ex) throws BuildException {
		if (failOnError) {
			throw new BuildException(ex);
		} else {
			System.out.println(ex.getMessage());
		}
	}

	// Setter methods
	public void setCommand(String command) {
		this.command = command;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public void setVolName(String volName) {
		this.volumeName = volName;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setSizeBlocks(String sizeBlocks) {
		this.sizeBlocks = sizeBlocks;
	}

	public void setFailOnError(String failOnError) {
		this.failOnError = Boolean.parseBoolean(failOnError);
	}
}
