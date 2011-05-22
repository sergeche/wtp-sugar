package ru.chikuyonok.wtp.sugar.provider;

import java.io.File;

import org.eclipse.swt.graphics.Image;

import ru.chikuyonok.wtp.sugar.FileUtils;

public class FileProposal {
	private File file;
	private String parentPath;
	private Image image;
	
	public FileProposal(File file, String parentPath) {
		this.file = file;
		this.parentPath = parentPath;
	}
	
	public FileProposal(File file, String parentPath, Image image) {
		this(file, parentPath);
		this.image = image;
	}
	
	public File getFile() {
		return file;
	}
	
	public String getRelativePath(boolean addTrailingSlash) {
		String path = FileUtils.makeFilePathRelative(new File(parentPath), file);
		if (addTrailingSlash && !path.startsWith("/"))
			path = "/" + path;
		return path.replaceAll("\\\\", "/");
	}
	
	public Image getImage() {
		return image;
	}
}
