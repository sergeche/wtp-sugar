package ru.chikuyonok.wtp.sugar.properties;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

import ru.chikuyonok.wtp.sugar.dialogs.StatusInfo;
import ru.chikuyonok.wtp.sugar.dialogs.TypedElementSelectionValidator;
import ru.chikuyonok.wtp.sugar.dialogs.TypedViewerFilter;

public class DocumentRootPropertyPage extends PropertyPage {
	private static final String P_DOCUMENT_ROOT = "docRoot";
	private static final String DEFAULT_DOCUMENT_ROOT = "";
	protected IWorkspaceRoot workspaceRoot;
	private IProject project;
	
	protected IStatus errorStatus = new StatusInfo(IStatus.ERROR, "Please select one folder");
    protected IStatus okStatus = new StatusInfo();
    protected StringButtonFieldEditor docRootField;


	/**
	 * Constructor for SamplePropertyPage.
	 */
	public DocumentRootPropertyPage() {
		super();
		setMessage("Pick document root for your web files");
	}

	private void addFilePicker(Composite parent) {
		Composite composite = createDefaultComposite(parent);
		
		docRootField = new StringButtonFieldEditor(P_DOCUMENT_ROOT, "Document root", composite) {
			@Override
			protected String changePressed() {
				String curr = docRootField.getStringValue();
	            IPath currPath = null;
	            if (curr != null && curr.length() > 0) {
	                currPath = new Path(curr);
	            }
	            IPath destPath = openFolderDialog(currPath);
	            

	            if (destPath == null) {
	                return null;
	            } else {
	            	return destPath.toPortableString();
	            }
			}
		};
		
		docRootField.setChangeButtonText("Browse...");
		
		//Populate document root text field
		docRootField.setStringValue(getDocumentRoot((IResource) getElement()));
	}
	
	public static String getDocumentRoot(IResource project) {
		try {
			String docRoot =
				project.getPersistentProperty(
					new QualifiedName("", P_DOCUMENT_ROOT));
			return (docRoot != null) ? docRoot : DEFAULT_DOCUMENT_ROOT;
		} catch (CoreException e) {
			return DEFAULT_DOCUMENT_ROOT;
		}
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		
		workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		project = (IProject) getElement();
		
		addFilePicker(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performDefaults() {
		super.performDefaults();
		// Populate the owner text field with the default value
		docRootField.setStringValue(DEFAULT_DOCUMENT_ROOT);
	}
	
	public boolean performOk() {
		// store the value in the owner text field
		try {
			((IResource) getElement()).setPersistentProperty(
				new QualifiedName("", P_DOCUMENT_ROOT),
				docRootField.getStringValue());
		} catch (CoreException e) {
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	private IPath openFolderDialog(IPath path) {
        Class[] acceptedClasses = new Class[] { IFolder.class, IProject.class };
        ISelectionStatusValidator validator = new TypedElementSelectionValidator(
                acceptedClasses, false) {
            public IStatus validate(Object[] elements) {
                if (elements.length > 1 || elements.length == 0
                        || (elements[0] instanceof IFile)) {
                    return errorStatus;
                }
                return okStatus;
            }
        };

        IProject[] allProjects = workspaceRoot.getProjects();
        ArrayList<IProject> rejectedElements = new ArrayList<IProject>(allProjects.length);
        for (int i = 0; i < allProjects.length; i++) {
            if (!allProjects[i].equals(project)) {
                rejectedElements.add(allProjects[i]);
            }
        }
        ViewerFilter filter = new TypedViewerFilter(acceptedClasses, rejectedElements.toArray());

        ILabelProvider lp = new WorkbenchLabelProvider();
        ITreeContentProvider cp = new WorkbenchContentProvider();

        IResource initSelection = null;
        if (path != null) {
            initSelection = project.findMember(path);
        }

        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), lp, cp);
        dialog.setTitle("Folders");
        dialog.setValidator(validator);
        dialog.setMessage("Select folder that will be used as document root");
        dialog.addFilter(filter);
        dialog.setInput(workspaceRoot);
        dialog.setInitialSelection(initSelection);
        dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));

        if (dialog.open() == Window.OK) {
            return ((IResource) dialog.getFirstResult()).getProjectRelativePath();
        }
        return null;
    }

}