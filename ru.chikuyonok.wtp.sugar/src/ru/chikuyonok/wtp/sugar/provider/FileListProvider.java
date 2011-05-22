package ru.chikuyonok.wtp.sugar.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;

import ru.chikuyonok.wtp.sugar.FileUtils;
import ru.chikuyonok.wtp.sugar.StringUtils;
import ru.chikuyonok.wtp.sugar.properties.DocumentRootPropertyPage;

@SuppressWarnings("restriction")
public class FileListProvider {
	private volatile static FileListProvider singleton;
	
	private ArrayList<HTMLTagFileAssistPattern> namedPatterns;
	private CommonTagFileAssistPattern commonPattern;

	private HashMap<String, Image> imageCache;
	private IFileEditorMapping[] fileMappings;
	protected Map<IEditorDescriptor, Image> editorsToImages;
	
	public static String[] imagePatterns = new String[] { "*.jpg", "*.png", "*.gif" };
	public static String[] allPattern = new String[] { "*.*" };
	
	private FileListProvider() {
		// fill fields with common patterns
		String[] src = new String[] { "src" };
		
		namedPatterns = new ArrayList<HTMLTagFileAssistPattern>();
		namedPatterns.add(new HTMLTagFileAssistPattern(new String[] { "link" },
				new String[] { "href" }, new String[] { "*.css" }));
		namedPatterns.add(new HTMLTagFileAssistPattern(new String[] { "script" },
				src, new String[] { "*.js" }));
		namedPatterns.add(new HTMLTagFileAssistPattern(new String[] { "img" },
				src, imagePatterns));
		namedPatterns.add(new HTMLTagFileAssistPattern(new String[] { "video" },
				new String[] { "poster" }, imagePatterns));
		namedPatterns.add(new HTMLTagFileAssistPattern(new String[] { "video" },
				src, allPattern));
		
		commonPattern = new CommonTagFileAssistPattern(new String[]{"src", "href"}, allPattern);
	}
	
	public static FileListProvider getSingleton() {
		if (singleton == null) {
			synchronized (FileListProvider.class) {
				if (singleton == null) {
					singleton = new FileListProvider();
				}
			}
		}
		return singleton;
	}
	
	/**
	 * Returns list of file patterns available for passed node
	 * @param node
	 * @return
	 */
	public String[] getPatters(IDOMAttr node) {
		ArrayList<String> patterns = new ArrayList<String>();
		for (int i = 0; i < namedPatterns.size(); i++) {
			if (namedPatterns.get(i).matches(node))
				patterns.addAll(namedPatterns.get(i).getPatterns());
		}
		
		if (patterns.size() == 0 && commonPattern.matches(node))
			patterns.addAll(commonPattern.getPatterns());
		
		return patterns.toArray(new String[0]);
	}
	
	public boolean isSupported(IDOMAttr node) {
		for (int i = 0; i < namedPatterns.size(); i++) {
			if (namedPatterns.get(i).matches(node))
				return true;
		}
		
		return commonPattern.matches(node);
	}
	
	/**
	 * Returns file list suitable for specified node
	 * @param node
	 * @return
	 */
	public FileProposal[] getFileList(String valuePrefix, IDOMAttr node) {
		String[] patterns = getPatters(node);
		
		if (patterns != null && patterns.length > 0) {
			return getFileList(valuePrefix, patterns);
		}
		
		return null;
	}
	
	public FileProposal[] getFileList(String valuePrefix, String[] patterns) {
		clearState();
		ArrayList<FileProposal> resultFiles = new ArrayList<FileProposal>();
		IEditorInput pathEditor = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow().getActivePage().getActiveEditor()
			.getEditorInput();


		String editorPath = FileUtils.getPathFromEditorInput(pathEditor);
		boolean isAbsolute = false;
		
		String currentPath = editorPath;
		String docRoot = getDocumentRootPath();
		if (valuePrefix != null) {
			String s = StringUtils.trimStringQuotes(valuePrefix);
			isAbsolute = s.startsWith("/");
			if (isAbsolute) {
				// using absolute path: get document root
				currentPath = docRoot + s;
			} else if (!"".equals(s)) { //$NON-NLS-1$
				File current = new File(currentPath);
		
				if (current.isDirectory()) {
					currentPath = currentPath + s;
				} else {
					currentPath = current.getParent().toString() + File.separator + s;
				}
			}
		}
		
		File[] files = FileUtils.getFilesInDirectory(new File(currentPath));
		
		ArrayList<File> filteredFiles = new ArrayList<File>();
		
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				if (f.isDirectory() || FileUtils.matchPatterns(f, patterns)) {
					filteredFiles.add(f);
				}
			}
		}
		
		if (filteredFiles.size() == 0)
			return new FileProposal[0];
		
		for (File f : filteredFiles) {
			if (f.getName().startsWith(".")) { //$NON-NLS-1$
				continue;
			}
			
			// Don't include the current file in the list
			if (f.toString().equals(editorPath)) {
				continue;
			}
			
			resultFiles.add(new FileProposal(f, isAbsolute ? docRoot : editorPath, getFileIcon(f)));
		}
		
		return resultFiles.toArray(new FileProposal[0]);
	}
	
	/**
	 * 
	 * 
	 * @param valuePrefix
	 * @param beginOffset
	 * @param replaceLength
	 */
	public ICompletionProposal[] getFileCompletionProposals(String[] patterns,
			String valuePrefix, int beginOffset, int replaceLength) {
		
		ArrayList<CompletionProposal> completionProposals = new ArrayList<CompletionProposal>();
		FileProposal[] files = FileListProvider.getSingleton().getFileList(valuePrefix, patterns);
		
		if (files == null) {
			return new ICompletionProposal[0];
		}
		
		boolean isEmptyPrefix = valuePrefix == null || "".equals(valuePrefix);
		boolean isAbsolute = valuePrefix.startsWith("/");

		for (int i = 0; i < files.length; i++) {
			FileProposal f = files[i];
			String replaceString = f.getRelativePath(isAbsolute);
			
			// filter results that doesn't starts with value prefix
			if (!isEmptyPrefix && !replaceString.startsWith(valuePrefix)) {
				continue;
			}
		
			CompletionProposal cp = new CompletionProposal(replaceString,
					beginOffset, replaceLength, replaceString.length(), 
					f.getImage(),
					FileUtils.getPathFromURI(replaceString), null, null);

			if (cp != null) {
				completionProposals.add(cp);
			}
		}
		return completionProposals.toArray(new ICompletionProposal[0]);
	}
	
	/**
	 * @return path to document root (may differ from project root)
	 */
	public String getDocumentRootPath() {
		IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editorPart != null) {
			IFileEditorInput input = (IFileEditorInput)editorPart.getEditorInput();
		    IFile file = input.getFile();
		    IProject activeProject = file.getProject();
		    
		    String docRootSuffix = DocumentRootPropertyPage.getDocumentRoot(activeProject);
		    
		    if (docRootSuffix.startsWith("/"))
		    	docRootSuffix = docRootSuffix.replaceFirst("^/+", "");
		    
		    return activeProject.getLocation().append(docRootSuffix).removeTrailingSeparator().toOSString();
		}
		
		return "";
	}
	
	public void clearState() {
		if (imageCache != null) {
			// dispose all cached images
			for (Iterator<Image> iterator = imageCache.values().iterator(); iterator.hasNext();) {
				Image img = (Image) iterator.next();
				if (img != null && !img.isDisposed())
					img.dispose();
			}
			
			imageCache.clear();
		} else {
			imageCache = new HashMap<String, Image>();
		}
		
		if (editorsToImages != null) {
            for (Iterator<Image> e = editorsToImages.values().iterator(); e.hasNext();) {
                ((Image) e.next()).dispose();
            }
            editorsToImages = null;
        }
		
		fileMappings = null;
		editorsToImages = new HashMap<IEditorDescriptor, Image>(50);
	}
	
	public Image getFileIcon(File file) {
		IWorkbench wb = PlatformUI.getWorkbench();
		
		if (file.isDirectory())
			return wb.getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		
		String ext = FileUtils.getExtension(file);
		if (ext == null)
			return null;
		
		if (fileMappings == null)
			fileMappings = wb.getEditorRegistry().getFileEditorMappings();
		
		if (!imageCache.containsKey(ext)) {
			Image img = null;
			for (int i = 0; i < fileMappings.length; i++) {
				IFileEditorMapping fm = fileMappings[i];
				if (ext.equals(fm.getExtension())) {
					IEditorDescriptor editor = fm.getDefaultEditor();
					
					if (editor != null) {
						img = getEditorImage(editor);
					} else if (fm.getEditors().length > 0) {
						img = getEditorImage(fm.getEditors()[0]);
					}
					
					break;
				}
			}
			
			if (img == null)
				img = getContentTypeImage(ext);
			
			imageCache.put(ext, img);
		}
		
		if (imageCache.get(ext) == null) {
			// provide default icon
			return wb.getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		}
		
		return imageCache.get(ext);
	}
	
	/**
     * Returns the image associated with the given editor.
     */
    protected Image getEditorImage(IEditorDescriptor editor) {
        Image image = (Image) editorsToImages.get(editor);
        if (image == null) {
            image = editor.getImageDescriptor().createImage();
            editorsToImages.put(editor, image);
        }
        return image;
    }
    
    protected Image getContentTypeImage(String ext) {
    	EditorRegistry registry = (EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry();
    	
    	IContentType[] contentTypes = Platform.getContentTypeManager()
    		.findContentTypesFor("*." + ext);
    	
    	for (int i = 0; i < contentTypes.length; i++) {
			IEditorDescriptor[] array = registry.getEditorsForContentType(contentTypes[i]);
			if (array.length > 0)
				return getEditorImage(array[0]);
		}
    	
    	return null;
    }
}
