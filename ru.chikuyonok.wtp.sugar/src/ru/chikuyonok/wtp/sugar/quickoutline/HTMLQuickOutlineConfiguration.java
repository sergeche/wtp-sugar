package ru.chikuyonok.wtp.sugar.quickoutline;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.eclipse.wst.xml.ui.internal.quickoutline.XMLQuickOutlineConfiguration;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ru.chikuyonok.wtp.sugar.contentoutline.OutlineLabelProvider;

@SuppressWarnings("restriction")
public class HTMLQuickOutlineConfiguration extends
		XMLQuickOutlineConfiguration {
	
	private class HTMLLabelProvider extends JFaceNodeLabelProvider {
		public String getText(Object element) {
			if (element instanceof Node) {
				Node node = (Node) element;
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					return OutlineLabelProvider.getHTMLLabel((Element) node);
				}
			}
			return super.getText(element);
		}
	}
	
	public ILabelProvider getLabelProvider() {
		return new HTMLLabelProvider();
	}
}
