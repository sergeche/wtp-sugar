package ru.chikuyonok.wtp.sugar.provider;

import java.util.ArrayList;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;

@SuppressWarnings("restriction")
public class CommonTagFileAssistPattern {
	protected ArrayList<String> fAttributes;
	protected ArrayList<String> fPatterns;
	
	public CommonTagFileAssistPattern() {
		fAttributes = new ArrayList<String>();
		fPatterns = new ArrayList<String>();
	}
	
	public CommonTagFileAssistPattern(String[] attrs, String[] patterns) {
		this();
		addAttributes(attrs);
		addPatterns(patterns);
	}
	
	/**
	 * Add attribute name that should receive file content assist
	 * @param name
	 */
	public void addAttribute(String name) {
		name = name.toLowerCase();
		if (!fAttributes.contains(name))
			fAttributes.add(name);
	}
	
	/**
	 * Add attribute names that should receive file content assist
	 * @param name
	 */
	public void addAttributes(String[] names) {
		for (int i = 0; i < names.length; i++) {
			addAttribute(names[i]);
		}
	}
	
	/**
	 * Add file pattern that should filter file content assist proposals
	 * for specified tag attributes
	 * @param name
	 */
	public void addPattern(String name) {
		if (!fPatterns.contains(name))
			fPatterns.add(name);
	}
	
	/**
	 * Add file pattern that should filter file content assist proposals
	 * for specified tag attributes
	 * @param name
	 */
	public void addPatterns(String[] names) {
		for (int i = 0; i < names.length; i++) {
			addPattern(names[i]);
		}
	}
	
	public ArrayList<String> getPatterns() {
		return fPatterns;
	}
	
	/**
	 * Check if passed node matches current tag and attribute criteria
	 * @param node
	 * @return
	 */
	public boolean matches(IDOMAttr node) {
		return fAttributes.contains(node.getName().toLowerCase());
	}
}
