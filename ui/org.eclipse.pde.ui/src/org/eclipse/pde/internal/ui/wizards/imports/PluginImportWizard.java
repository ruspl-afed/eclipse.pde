/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.pde.internal.ui.wizards.imports;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.WorkspaceModelManager;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModelBase;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.wizards.imports.PluginImportOperation.IReplaceQuery;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class PluginImportWizard extends Wizard implements IImportWizard {

	private static final String STORE_SECTION = "PluginImportWizard";
	private static final String KEY_WTITLE = "ImportWizard.title";
	private static final String KEY_NO_TO_ALL_LABEL = "ImportWizard.noToAll";
	private static final String KEY_MESSAGES_TITLE = "ImportWizard.messages.title";
	private static final String KEY_MESSAGES_NO_PLUGINS =
		"ImportWizard.messages.noPlugins";
	private static final String KEY_MESSAGES_DO_NOT_ASK =
		"ImportWizard.messages.doNotAsk";
	private static final String KEY_MESSAGES_EXISTS =
		"ImportWizard.messages.exists";

	private PluginImportWizardFirstPage page1;
	private PluginImportWizardDetailedPage page2;
	private HashSet preSelectedModels = new HashSet();

	public PluginImportWizard() {
		IDialogSettings masterSettings = PDEPlugin.getDefault().getDialogSettings();
		setDialogSettings(getSettingsSection(masterSettings));
		setDefaultPageImageDescriptor(PDEPluginImages.DESC_NEWEXPRJ_WIZ);
		setWindowTitle(PDEPlugin.getResourceString(KEY_WTITLE));
	}

	/*
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		Object[] items = selection.toArray();
		WorkspaceModelManager wManager = PDECore.getDefault().getWorkspaceModelManager();
		for (int i = 0; i < items.length; i++) {
			IProject project = null;
			if (items[i] instanceof IProject) {
				project = (IProject) items[i];
			} else if (items[i] instanceof IJavaProject) {
				project = ((IJavaProject)items[i]).getProject();
			} else if (items[i] instanceof IFile) {
				IFile file = (IFile) items[i];
				if (file.getName().equals("plugin.xml") || file.getName().equals("fragment.xml")) {
					project = file.getProject();
				}
			}
			if (project != null) {
				IModel model = wManager.getWorkspaceModel(project);
				if (model != null && model instanceof WorkspacePluginModelBase) {
					preSelectedModels.add(
						((WorkspacePluginModelBase) model).getPluginBase().getId());
				}
			}
		}
	}

	/*
	 * @see org.eclipse.jface.wizard.IWizard#addPages
	 */
	public void addPages() {
		setNeedsProgressMonitor(true);

		page1 = new PluginImportWizardFirstPage();
		addPage(page1);
		page2 = new PluginImportWizardDetailedPage(page1, preSelectedModels);
		addPage(page2);
	}

	private IDialogSettings getSettingsSection(IDialogSettings master) {
		IDialogSettings setting = master.getSection(STORE_SECTION);
		if (setting == null) {
			setting = master.addNewSection(STORE_SECTION);
		}
		return setting;
	}

	/*
	 * @see Wizard#performFinish()
	 */
	public boolean performFinish() {
		final ArrayList modelIds = new ArrayList();
		try {
			final IPluginModelBase[] models = page2.getSelectedModels();
			if (models.length == 0) {
				MessageDialog.openInformation(
					getShell(),
					PDEPlugin.getResourceString(KEY_MESSAGES_TITLE),
					PDEPlugin.getResourceString(KEY_MESSAGES_NO_PLUGINS));
				return false;
			}

			page1.storeSettings(true);
			page2.storeSettings(true);
			
			
			IRunnableWithProgress op =
				getImportOperation(
					getShell(),
					page1.doImportToWorkspace(),
					page1.doExtractPluginSource(),
					models,
					modelIds);
			//start = System.currentTimeMillis();
			getContainer().run(true, true, op);

		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			PDEPlugin.logException(e);
			return true; // exception handled
		}
		
		return true;
	}
	
	public static IRunnableWithProgress getImportOperation(
		final Shell shell,
		final boolean doImport,
		final boolean doExtract,
		final IPluginModelBase[] models,
		final ArrayList modelIds) {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
				boolean isAutoBuilding = PDEPlugin.getWorkspace().isAutoBuilding();
				try {
					int numUnits = 2;
					if (isAutoBuilding) {
						IWorkspace workspace = PDEPlugin.getWorkspace();
						IWorkspaceDescription description = workspace.getDescription();
						description.setAutoBuilding(false);
						workspace.setDescription(description);
						numUnits += 1;
					}
					monitor.beginTask("", numUnits);
					IReplaceQuery query = new ReplaceQuery(shell);
					PluginImportOperation op =
						new PluginImportOperation(
							models,
							modelIds,
							doImport,
							doExtract,
							query);
					PDEPlugin.getWorkspace().run(op, new SubProgressMonitor(monitor, 1));
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} catch (OperationCanceledException e) {
					throw new InterruptedException(e.getMessage());
				} finally {
					try {
						PDEPlugin.getWorkspace().run(
							getUpdateClasspathOperation(modelIds),
							new SubProgressMonitor(monitor, 1));
						if (isAutoBuilding) {
							IWorkspace workspace = PDEPlugin.getWorkspace();
							IWorkspaceDescription description =
								workspace.getDescription();
							description.setAutoBuilding(true);
							workspace.setDescription(description);
							PDEPlugin.getWorkspace().build(
								IncrementalProjectBuilder.INCREMENTAL_BUILD,
								new SubProgressMonitor(monitor, 1));
						}

					} catch (CoreException e) {
					}
					monitor.done();
				}
			}
		};
	}
	
	private static IWorkspaceRunnable getUpdateClasspathOperation(final ArrayList modelIds) {
		return new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				UpdateClasspathAction.doUpdateClasspath(
					monitor,
					getWorkspaceCounterparts(modelIds));
			}

		};
	}

	/*
	 * @see Wizard#performCancel()
	 */
	public boolean performCancel() {
		page1.storeSettings(false);
		page2.storeSettings(false);
		return super.performCancel();
	}

	private static class ReplaceDialog extends MessageDialog {
		public ReplaceDialog(Shell parentShell, String dialogMessage) {
			super(
				parentShell,
				PDEPlugin.getResourceString(KEY_MESSAGES_TITLE),
				null,
				dialogMessage,
				MessageDialog.QUESTION,
				new String[] {
					IDialogConstants.YES_LABEL,
					IDialogConstants.YES_TO_ALL_LABEL,
					IDialogConstants.NO_LABEL,
					PDEPlugin.getResourceString(KEY_NO_TO_ALL_LABEL),
					IDialogConstants.CANCEL_LABEL },
				0);
		}
	}

	private static class ReplaceQuery implements IReplaceQuery {
		private Shell shell;
		public ReplaceQuery(Shell shell) {
			this.shell = shell;
		}

		private int yesToAll = 0;
		private int[] RETURNCODES =
			{
				IReplaceQuery.YES,
				IReplaceQuery.YES,
				IReplaceQuery.NO,
				IReplaceQuery.NO,
				IReplaceQuery.CANCEL };

		public int doQuery(IProject project) {
			if (yesToAll != 0) {
				return yesToAll > 0 ? IReplaceQuery.YES : IReplaceQuery.NO;
			}

			final String message =
				PDEPlugin.getFormattedMessage(KEY_MESSAGES_EXISTS, project.getName());
			final int[] result = { IReplaceQuery.CANCEL };
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					ReplaceDialog dialog = new ReplaceDialog(shell, message);
					int retVal = dialog.open();
					if (retVal >= 0) {
						result[0] = RETURNCODES[retVal];
						if (retVal == 1) {
							yesToAll = 1;
						} else if (retVal == 3) {
							yesToAll = -1;
						}
					}
				}
			});
			return result[0];
		}
	}
	
	private static IPluginModelBase[] getWorkspaceCounterparts(ArrayList modelIds) {
		
		IPluginModelBase[] allModels = PDECore.getDefault().getWorkspaceModelManager().getAllModels();
		ArrayList desiredModels = new ArrayList();
		for (int i = 0; i < allModels.length; i++) {
			if (modelIds.contains(allModels[i].getPluginBase().getId()))
				desiredModels.add(allModels[i]);				
		}
		
		return (IPluginModelBase[])desiredModels.toArray(new IPluginModelBase[desiredModels.size()]);
	}
}