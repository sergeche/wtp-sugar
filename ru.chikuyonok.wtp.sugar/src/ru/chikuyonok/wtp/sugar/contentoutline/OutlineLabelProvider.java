package ru.chikuyonok.wtp.sugar.contentoutline;

import org.w3c.dom.Element;

public class OutlineLabelProvider {
	private static String XSL_TEMPLATE = "xsl:template";
	private static String XSL_APPLY_TEMPLATES = "xsl:apply-templates";
	private static String XSL_CALL_TEMPLATE = "xsl:call-template";
	
	public static String getXSLLabel(Element elem) {
		StringBuffer text = new StringBuffer(elem.getLocalName());
		
		String attr = elem.getAttribute("name");
		if (attr != null && !"".equals(attr)) {
			text.append(" : " + attr);
		}
		
		attr = elem.getAttribute("select");
		if (attr != null && !"".equals(attr)) {
			text.append(" : " + attr);
		}
		
		attr = elem.getAttribute("match");
		if (attr != null && !"".equals(attr)) {
			text.append(" : " + attr);
		}
		
		attr = elem.getAttribute("mode");
		if (attr != null && !"".equals(attr)) {
			text.append(" [" + attr + "]");
		}
		
		return text.toString();
	}
	
	public static String getHTMLLabel(Element elem) {
		StringBuilder text = new StringBuilder(elem.getNodeName());
		
		String attr = elem.getAttribute("id");
		if (attr != null && !"".equals(attr)) {
			text.append("#" + attr);
		}
		
		attr = elem.getAttribute("class");
		if (attr != null && !"".equals(attr)) {
			text.append("." + attr.trim().replaceAll("\\s+", "."));
		}
		
		return text.toString();
	}
	
	public static boolean isXSLTemplate(Element elem) {
		String name = elem.getNodeName().toLowerCase();
		return name.equals(XSL_APPLY_TEMPLATES) || name.equals(XSL_CALL_TEMPLATE) || name.equals(XSL_TEMPLATE);
	}
}
