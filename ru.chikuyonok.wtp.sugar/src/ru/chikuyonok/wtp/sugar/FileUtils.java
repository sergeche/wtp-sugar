/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package ru.chikuyonok.wtp.sugar;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public class FileUtils {

	private static final String FILE_COLON = "file:"; //$NON-NLS-1$
	private static final String FILE_SLASH = FILE_COLON + "/"; //$NON-NLS-1$
	private static final String FILE_SLASH_SLASH = FILE_SLASH + "/"; //$NON-NLS-1$

	/**
	 * Returns all files in the current directory.
	 * 
	 * @param file
	 *            The file to grab files in reference to
	 * @return A list of files, or a empty directory
	 */
	public static File[] getFilesInDirectory(File file) {
		Path path = new Path(file.toString());
		String lastSegment = path.lastSegment();

		File[] files = new File[0];

		if (file.isDirectory()) {
			files = file.listFiles();
		} else {
			File parent = file.getParentFile();
			files = parent.listFiles();
		}

		if (lastSegment != null && lastSegment.indexOf('*') >= 0) {
			return matchFiles(lastSegment, files);
		} else {
			return files;
		}
	}

	/**
	 * Given a list of files and a regular expression pattern, return a list of
	 * files that match the pattern
	 * 
	 * @param pattern
	 *            The pattern to check. Currently only really works with
	 *            patterns like *.js
	 * @param files
	 *            The list of files
	 * @return The filtered list
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static File[] matchFiles(String pattern, File[] files) {
		if (pattern == null || files == null || files.length < 1)
			return new File[0];
		ArrayList al = new ArrayList();
		for (int i = 0; i < files.length; i++) {
			File fileTest = files[i];
			if (fileTest.toString().matches(preprocessPattern(pattern))) {
				al.add(fileTest);
			}
		}
		return (File[]) al.toArray(new File[0]);
	}
	
	public static boolean matchPatterns(File file, String[] patterns) {
		if (patterns == null || patterns.length == 0 || file == null)
			return false;
		
		for (int i = 0; i < patterns.length; i++) {
			if (file.toString().matches(preprocessPattern(patterns[i])))
				return true;
		}
		
		return false;
	}
	
	private static String preprocessPattern(String pattern) {
		String newPattern = StringUtils.replace(pattern, "\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
		newPattern = StringUtils.replace(newPattern, ".", "\\."); //$NON-NLS-1$ //$NON-NLS-2$
		return StringUtils.replace(newPattern, "*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns a relative path for the second file compared to the first
	 * 
	 * @param fileA
	 *            The "reference" file path
	 * @param fileB
	 *            The file to make relative
	 * @return String
	 */
	public static String makeFilePathRelative(File fileA, File fileB) {
		String separator = System.getProperty("file.separator"); //$NON-NLS-1$

		String a = fileA.toString();
		if (!fileA.isDirectory()) {
			a = fileA.getParent().toString() + separator;
		}

		String b = fileB.toString();
		if (fileB.isDirectory()) {
			b = b + separator;
		}

		String r = StringUtils.replace(b, a, StringUtils.EMPTY);
		if (r.endsWith(separator)) {
			r = r.substring(0, r.length() - 1);
		}

		return r;
	}

	/**
	 * Calls IFile.getLocation if it exists and uses an Eclipse internal
	 * mechanism if the file is deleted. Look at the implementation of
	 * IFile.getLocation to see why this is necessary. Basically getLocation()
	 * returns null if the enclosing project doesn't exist so this allows the
	 * location of a deleted file to be found.
	 * 
	 * @param file
	 * @return - Absolute OS string of file location
	 */
	public static String getStringOfIFileLocation(IFile file) {
		String location = null;
		IPath path = getPathOfIFileLocation(file);
		if (path != null) {
			location = path.makeAbsolute().toOSString();
		}
		return location;
	}

	/**
	 * @see com.aptana.ide.core.ui.CoreUIUtils#getStringOfIFileLocation(IFile
	 *      file)
	 * @param file
	 * @return - path of IFile
	 */
	public static IPath getPathOfIFileLocation(IFile file) {
		IPath location = null;
		if (file != null) {
			if (file.exists() && file.getProject() != null
					&& file.getProject().exists()) {
				location = file.getLocation();
			}
		}
		return location;
	}

	/**
	 * Returns the current path to the source file from an editor input.
	 * 
	 * @param input
	 *            the editor input
	 * @return the path, or null if not found
	 */
	public static String getPathFromEditorInput(IEditorInput input) {
		try {
			if (input instanceof FileEditorInput) {
				IFile file = ((FileEditorInput) input).getFile();
				return getStringOfIFileLocation(file);
			} else if (input instanceof IStorageEditorInput) {
				IStorageEditorInput sei = (IStorageEditorInput) input;
				try {
					return sei.getStorage().getFullPath().toOSString();
				} catch (Exception e) {
					if (input instanceof IPathEditorInput) {
						IPathEditorInput pin = (IPathEditorInput) input;
						return pin.getPath().toOSString();
					}
				}
			} else if (input instanceof IPathEditorInput) {
				IPathEditorInput pin = (IPathEditorInput) input;
				return pin.getPath().toOSString();
			}
		} catch (Exception e) {
			return null;
		}

		return null;
	}

	/**
	 * Returns the URI for the current editor (effectively the file path
	 * transformed into file://)
	 * 
	 * @param editor
	 * @return String
	 */
	public static String getURI(IEditorPart editor) {
		if (editor != null && editor.getEditorInput() != null) {
			return getURI(editor.getEditorInput());
		} else {
			return null;
		}
	}

	/**
	 * Returns a valid URI from the passed in editor input. This assumed that
	 * the editor input represents a file on disk
	 * 
	 * @param input
	 * @return String
	 */
	public static String getURI(IEditorInput input) {
		String s = getPathFromEditorInput(input);
		if (s == null) {
			try {
				Method method = input.getClass().getMethod("getURI"); //$NON-NLS-1$
				return ((URI) method.invoke(input)).toString();
			} catch (Exception e) {

			}
			return null;
		}
		return getURI(new File(s));
	}

	/**
	 * Returns a URI from a file
	 * 
	 * @param file
	 *            the file to pull from
	 * @return the string path to the file
	 */
	public static String getURI(File file) {
		return getURI(file, true);
	}

	/**
	 * Returns a URI from a file
	 * 
	 * @param file
	 *            the file to pull from
	 * @param urlEncode
	 *            do we url encode the file name
	 * @return the string path to the file
	 */
	public static String getURI(File file, boolean urlEncode) {
		String filePath = null;

		String path = file.getPath();
		if (path.startsWith("file:\\")) //$NON-NLS-1$
		{
			filePath = path.replaceAll("file:\\\\", FILE_SLASH_SLASH); //$NON-NLS-1$
		} else if (path.startsWith("http:\\")) //$NON-NLS-1$
		{
			filePath = path.replaceAll("http:\\\\", "http://"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			try {
				filePath = file.getCanonicalPath();
			} catch (IOException e) {
				filePath = file.getAbsolutePath();
			}

			if (filePath.startsWith("\\\\")) //$NON-NLS-1$
			{
				filePath = filePath.substring(2);
			}
			filePath = appendProtocol(filePath);
		}

		filePath = filePath.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$

		if (urlEncode) {
			filePath = urlEncodeFilename(filePath.toCharArray());
		}

		URI uri;
		try {
			if (urlEncode) {
				uri = new URI(filePath).normalize();
				return uri.toString();
			} else {
				return filePath;
			}
		} catch (URISyntaxException e) {
			return filePath;
		}
	}

	/**
	 * Appends the file:// protocol, if none found
	 * 
	 * @param path
	 * @return String
	 */
	public static String appendProtocol(String path) {
		if (path.indexOf("://") < 0) //$NON-NLS-1$
		{
			return FILE_SLASH_SLASH + path;
		}
		return path;
	}

	/**
	 * This method encodes the URL, removes the spaces and brackets from the URL
	 * and replaces the same with <code>"%20"</code> and
	 * <code>"%5B" and "%5D"</code> and <code>"%7B" "%7D"</code>.
	 * 
	 * @param input
	 * @return String
	 * @since 3.0.2
	 */
	public static String urlEncodeFilename(char[] input) {

		if (input == null) {
			return null;
		}

		StringBuffer retu = new StringBuffer(input.length);
		for (int i = 0; i < input.length; i++) {
			if (input[i] == ' ') {
				retu.append("%20"); //$NON-NLS-1$
			} else if (input[i] == '[') {
				retu.append("%5B"); //$NON-NLS-1$
			} else if (input[i] == ']') {
				retu.append("%5D"); //$NON-NLS-1$
			} else if (input[i] == '{') {
				retu.append("%7B"); //$NON-NLS-1$
			} else if (input[i] == '}') {
				retu.append("%7D"); //$NON-NLS-1$
			} else if (input[i] == '`') {
				retu.append("%60"); //$NON-NLS-1$
			} else if (input[i] == '+') {
				retu.append("%2B"); //$NON-NLS-1$
			} else {
				retu.append(input[i]);
			}
		}
		return retu.toString();
	}

	/**
	 * Gets the file path from a URI
	 * 
	 * @param sourceURI
	 *            the source URI
	 * @return the URI converted to a path (removed file:// from the beginning)
	 */
	public static String getPathFromURI(String sourceURI) {
		String uri = sourceURI;

		if (sourceURI.startsWith(FILE_SLASH_SLASH)) {
			uri = sourceURI.substring(FILE_SLASH_SLASH.length());
		}
		if (sourceURI.startsWith(FILE_COLON)) {
			uri = sourceURI.substring(FILE_COLON.length());
		}

		try {
			return URLDecoder.decode(uri, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			// TODO log
		}

		return uri;
	}
	
	public static String getExtension(File file) {
		String name = file.toString();
		int mid = name.lastIndexOf(".");
		if (mid == -1)
			return null;
		else
			return name.substring(mid + 1);
	}
}
