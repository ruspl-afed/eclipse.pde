/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.ui.shared.target;

import org.eclipse.pde.internal.core.target.FeatureBundleContainer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.core.target.provisional.IBundleContainer;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.SWTFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page for editing a feature bundle container, currently none of the options can be changed
 *
 */
public class EditFeatureContainerPage extends EditDirectoryContainerPage {

	public EditFeatureContainerPage(IBundleContainer container) {
		super(container);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.shared.target.EditDirectoryContainerPage#getDefaultTitle()
	 */
	protected String getDefaultTitle() {
		return Messages.EditFeatureContainerPage_0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.shared.target.EditDirectoryContainerPage#getDefaultMessage()
	 */
	protected String getDefaultMessage() {
		return Messages.EditFeatureContainerPage_1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.shared.target.AddDirectoryContainerPage#createLocationArea(org.eclipse.swt.widgets.Composite)
	 */
	protected void createLocationArea(Composite parent) {
		FeatureBundleContainer container = (FeatureBundleContainer) getBundleContainer();
		Composite comp = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_HORIZONTAL);

		SWTFactory.createLabel(comp, Messages.EditFeatureContainerPage_2, 1);
		Text text = SWTFactory.createText(comp, SWT.READ_ONLY | SWT.BORDER, 1);
		text.setText(container.getFeatureId());

		SWTFactory.createLabel(comp, Messages.EditFeatureContainerPage_3, 1);
		text = SWTFactory.createText(comp, SWT.READ_ONLY | SWT.BORDER, 1);
		text.setText(container.getFeatureVersion() != null ? container.getFeatureVersion() : Messages.EditFeatureContainerPage_4);

		SWTFactory.createLabel(comp, Messages.EditFeatureContainerPage_5, 1);
		text = SWTFactory.createText(comp, SWT.READ_ONLY | SWT.BORDER, 1);
		try {
			text.setText(container.getLocation(false));
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
			PDEPlugin.log(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.shared.target.EditDirectoryContainerPage#initializeInputFields(org.eclipse.pde.internal.core.target.provisional.IBundleContainer)
	 */
	protected void initializeInputFields(IBundleContainer container) {
		containerChanged(0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.shared.target.EditDirectoryContainerPage#storeSettings()
	 */
	public void storeSettings() {
		// Do nothing, no settings
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.shared.target.EditDirectoryContainerPage#validateInput()
	 */
	protected boolean validateInput() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.shared.target.EditDirectoryContainerPage#refreshContainer(org.eclipse.pde.internal.core.target.provisional.IBundleContainer)
	 */
	protected IBundleContainer createContainer(IBundleContainer previous) throws CoreException {
		return getBundleContainer();
	}

}
