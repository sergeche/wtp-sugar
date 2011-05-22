package ru.chikuyonok.wtp.sugar;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;

import ru.chikuyonok.wtp.sugar.provider.FileListProvider;

@SuppressWarnings("restriction")
public class FileCompletionProposalComputer implements ICompletionProposalComputer {
	
	private static final ICompletionProposal[] NO_PROPOSALS= new ICompletionProposal[0];
	private static final IContextInformation[] NO_CONTEXTS= new IContextInformation[0];
	
	@Override
	public void sessionStarted() {
		
	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(
			CompletionProposalInvocationContext context,
			IProgressMonitor monitor) {
		
		ICompletionProposal[] proposals = NO_PROPOSALS;
		
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof StructuredTextEditor) {
			ISelectionProvider selectionProvider = ((StructuredTextEditor) editor).getSelectionProvider();
			IStructuredSelection selection = (IStructuredSelection) selectionProvider.getSelection();
			Object curElem = selection.getFirstElement();
			if (curElem instanceof AttrImpl) {
				
				AttrImpl attr = (AttrImpl) curElem;
				
				// current attribute
				if (FileListProvider.getSingleton().isSupported(attr)) {
					String value = attr.getValueRegionText();
					int valuePos = attr.getValueRegionStartOffset();
					
					if (value == null) {
						// XXX unquoted attributes are not supported yet
						return Arrays.asList(NO_PROPOSALS);
					}
					
					if (value.startsWith("'") || value.startsWith("\"")) {
						valuePos++;
					}
					
					value = StringUtils.trimStringQuotes(value);
					
					FileListProvider flp = FileListProvider.getSingleton();
					proposals = flp.getFileCompletionProposals(
							flp.getPatters(attr),
							value.substring(0, context.getInvocationOffset()
									- valuePos), valuePos, value.length());
				}
			}
		}
		
		return Arrays.asList(proposals);
	}

	@Override
	public List<IContextInformation> computeContextInformation(
			CompletionProposalInvocationContext context,
			IProgressMonitor monitor) {
		
//		System.out.println("Computing context info");
		return Arrays.asList(NO_CONTEXTS);
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public void sessionEnded() {
		FileListProvider.getSingleton().clearState();
	}
}
