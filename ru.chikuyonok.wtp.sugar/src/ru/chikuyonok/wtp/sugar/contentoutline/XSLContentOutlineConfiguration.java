package ru.chikuyonok.wtp.sugar.contentoutline;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.wst.xsl.ui.internal.contentoutline.AttributeShowingLabelProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class XSLContentOutlineConfiguration
		extends
		org.eclipse.wst.xsl.ui.internal.contentoutline.XSLContentOutlineConfiguration {
	private AttributeShowingLabelProvider fAttributeShowingLabelProvider;
	
	private class XSLLabelProvider extends AttributeShowingLabelProvider {
		private boolean fShowAttributes;

		public XSLLabelProvider(boolean showAttributes) {
			super(showAttributes);
			fShowAttributes = showAttributes;
		}
		
		public String getText(Object o) {
			StringBuffer text = null;
			if (o instanceof Node) {
				Node node = (Node) o;
				if ((node.getNodeType() == Node.ELEMENT_NODE) && fShowAttributes) {
					Element elem = (Element) node;
					if (OutlineLabelProvider.isXSLTemplate(elem)) {
						text = new StringBuffer(OutlineLabelProvider.getXSLLabel(elem));
					} else {
						text = new StringBuffer(super.getText(o));
					}
				} else {
					text = new StringBuffer(super.getText(o));
				}
			} else {
				return super.toString();
			}
			
			return text.toString();
		}
	}
	
	public XSLContentOutlineConfiguration() {
		super();
	}

	@Override
	public IContentProvider getContentProvider(TreeViewer viewer) {
		return super.getContentProvider(viewer);
	}
	
	@Override
	public ILabelProvider getLabelProvider(TreeViewer viewer) {
		if (fAttributeShowingLabelProvider == null) {
			fAttributeShowingLabelProvider = new XSLLabelProvider(true);
		}
		return fAttributeShowingLabelProvider;
	}
}
