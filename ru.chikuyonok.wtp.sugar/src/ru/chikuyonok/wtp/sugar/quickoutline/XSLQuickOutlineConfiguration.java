package ru.chikuyonok.wtp.sugar.quickoutline;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeContentProvider;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.eclipse.wst.xml.ui.internal.quickoutline.XMLQuickOutlineConfiguration;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ru.chikuyonok.wtp.sugar.contentoutline.OutlineLabelProvider;


@SuppressWarnings("restriction")
public class XSLQuickOutlineConfiguration extends
	XMLQuickOutlineConfiguration {
	
	private class XSLLabelProvider extends JFaceNodeLabelProvider {
		public String getText(Object element) {
			if (element instanceof Node) {
				Node node = (Node) element;
				if (node.getNodeType() == Node.ELEMENT_NODE 
						&& OutlineLabelProvider.isXSLTemplate((Element) node)) {
					return OutlineLabelProvider.getXSLLabel((Element) node);
				}
			}
			return super.getText(element);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.sse.ui.IOutlineContentManager#getContentProvider()
	 */
	public ITreeContentProvider getContentProvider() {
		return new JFaceNodeContentProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.sse.ui.IOutlineContentManager#getLabelProvider()
	 */
	public ILabelProvider getLabelProvider() {
		return new XSLLabelProvider();
	}
}
