package ru.chikuyonok.wtp.sugar.provider;

import java.util.ArrayList;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;

@SuppressWarnings("restriction")
public class HTMLTagFileAssistPattern extends CommonTagFileAssistPattern {
	protected ArrayList<String> fTags;
	
	public HTMLTagFileAssistPattern() {
		super();
		fTags = new ArrayList<String>();
	}
	
	public HTMLTagFileAssistPattern(String[] tags, String[] attrs, String[] patterns) {
		super(attrs, patterns);
		fTags = new ArrayList<String>();
		addTags(tags);
	}
	
	/**
	 * Add name of tag that should receive file content assist
	 * @param name
	 */
	public void addTag(String name) {
		name = name.toLowerCase();
		if (!fTags.contains(name))
			fTags.add(name);
	}
	
	/**
	 * Add name of tag that should receive file content assist
	 * @param name
	 */
	public void addTags(String[] names) {
		for (int i = 0; i < names.length; i++) {
			addTag(names[i]);
		}
	}
	
	/**
	 * Check if passed node matches current tag and attribute criteria
	 * @param node
	 * @return
	 */
	public boolean matches(IDOMAttr node) {
		return fTags.contains(node.getOwnerElement().getNodeName().toLowerCase())
				&& super.matches(node);
	}
}
