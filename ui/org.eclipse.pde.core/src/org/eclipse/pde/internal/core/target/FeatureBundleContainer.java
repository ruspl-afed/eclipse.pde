/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.target;

import java.io.File;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.build.site.PluginPathFinder;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.ifeature.*;
import org.eclipse.pde.internal.core.target.provisional.*;

/**
 * A container of the bundles contained in a feature.
 * 
 * @since 3.5
 */
public class FeatureBundleContainer extends AbstractBundleContainer {

	/**
	 * Constant describing the type of bundle container 
	 */
	public static final String TYPE = "Feature"; //$NON-NLS-1$

	/**
	 * Feature symbolic name 
	 */
	private String fId;

	/**
	 * Feature version or <code>null</code>
	 */
	private String fVersion;

	/**
	 * Install location which may contain string substitution variables
	 */
	private String fHome;

	/**
	 * Constructs a new feature bundle container for the feature at the specified
	 * location. Plug-ins are resolved in the plug-ins directory of the given home
	 * directory. When version is unspecified, the most recent version is used.
	 * 
	 * @param home root directory containing the features directory which
	 *  may contain string substitution variables
	 * @param name feature symbolic name
	 * @param version feature version, or <code>null</code> if unspecified
	 */
	FeatureBundleContainer(String home, String name, String version) {
		fId = name;
		fVersion = version;
		fHome = home;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.core.target.impl.AbstractBundleContainer#getLocation(boolean)
	 */
	public String getLocation(boolean resolve) throws CoreException {
		if (resolve) {
			return resolveHomeLocation().toOSString();
		}
		return fHome;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.core.target.impl.AbstractBundleContainer#getType()
	 */
	public String getType() {
		return TYPE;
	}

	/**
	 * Returns the symbolic name of the feature this bundle container resolves from
	 * 
	 * @return string feature id (symbolic name)
	 */
	public String getFeatureId() {
		return fId;
	}

	/**
	 * Returns the version of the feature this bundle container resolves from if
	 * a version was specified.
	 * 
	 * @return string feature version or <code>null</code>
	 */
	public String getFeatureVersion() {
		return fVersion;
	}

	/**
	 * Returns the home location with all variables resolved as a path.
	 * 
	 * @return resolved home location
	 * @throws CoreException
	 */
	private IPath resolveHomeLocation() throws CoreException {
		return new Path(resolveVariables(fHome));
	}

	/**
	 * Resolves and returns the directory containing the feature.
	 * 
	 * @return feature directory
	 * @throws CoreException if unable to resolve
	 */
	private File resolveFeatureLocation() throws CoreException {
		IPath home = resolveHomeLocation();
		File[] featurePaths = PluginPathFinder.getFeaturePaths(home.toOSString());
		if (featurePaths.length == 0) {
			// no features are included with the install/home location
			IPath path = home.append("features"); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, NLS.bind(Messages.FeatureBundleContainer_0, path.toOSString())));
		}
		// if a specific version is specified, find it
		if (fVersion != null) {
			StringBuffer buf = new StringBuffer();
			String name = buf.append(fId).append("_").append(fVersion).toString(); //$NON-NLS-1$
			for (int i = 0; i < featurePaths.length; i++) {
				File feature = featurePaths[i];
				if (feature.getName().equals(name)) {
					return feature;
				}
			}
			throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, NLS.bind(Messages.FeatureBundleContainer_1, fId)));
		}
		// use most recent version
		List versions = new ArrayList();
		StringBuffer buf = new StringBuffer();
		String prefix = buf.append(fId).append("_").toString(); //$NON-NLS-1$
		for (int i = 0; i < featurePaths.length; i++) {
			String name = featurePaths[i].getName();
			if (name.startsWith(prefix)) {
				versions.add(featurePaths[i]);
			}
		}
		if (versions.isEmpty()) {
			throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, NLS.bind(Messages.FeatureBundleContainer_1, fId)));
		}
		Collections.sort(versions, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((File) o1).getName().compareTo(((File) o2).getName());
			}
		});
		return (File) versions.get(versions.size() - 1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.core.target.impl.AbstractBundleContainer#resolveBundles(org.eclipse.pde.internal.core.target.provisional.ITargetDefinition, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IResolvedBundle[] resolveBundles(ITargetDefinition definition, IProgressMonitor monitor) throws CoreException {
		IFeatureModel model = null;
		try {
			if (monitor.isCanceled()) {
				return new IResolvedBundle[0];
			}
			File location = resolveFeatureLocation();
			File manifest = new File(location, ICoreConstants.FEATURE_FILENAME_DESCRIPTOR);
			if (!manifest.exists() || !manifest.isFile()) {
				throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, NLS.bind(Messages.FeatureBundleContainer_2, fId)));
			}
			model = ExternalFeatureModelManager.createModel(manifest);
			if (model == null || !model.isLoaded()) {
				throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, NLS.bind(Messages.FeatureBundleContainer_2, fId)));
			}
			// search bundles in plug-ins directory
			ITargetPlatformService service = (ITargetPlatformService) PDECore.getDefault().acquireService(ITargetPlatformService.class.getName());
			if (service == null) {
				throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, Messages.FeatureBundleContainer_4));
			}
			File dir = new File(manifest.getParentFile().getParentFile().getParentFile(), "plugins"); //$NON-NLS-1$
			if (!dir.exists() || !dir.isDirectory()) {
				throw new CoreException(new Status(IStatus.ERROR, PDECore.PLUGIN_ID, NLS.bind(Messages.FeatureBundleContainer_5, fId)));
			}
			if (monitor.isCanceled()) {
				return new IResolvedBundle[0];
			}

			IBundleContainer container = service.newDirectoryContainer(dir.getAbsolutePath());
			container.resolve(definition, monitor);
			IResolvedBundle[] bundles = container.getBundles();
			IFeature feature = model.getFeature();
			IFeaturePlugin[] plugins = feature.getPlugins();
			List matchInfos = new ArrayList(plugins.length);
			for (int i = 0; i < plugins.length; i++) {
				if (monitor.isCanceled()) {
					return new IResolvedBundle[0];
				}
				IFeaturePlugin plugin = plugins[i];
				// only include if plug-in matches environment
				if (isMatch(definition.getArch(), plugin.getArch(), Platform.getOSArch()) && isMatch(definition.getNL(), plugin.getNL(), Platform.getNL()) && isMatch(definition.getOS(), plugin.getOS(), Platform.getOS()) && isMatch(definition.getWS(), plugin.getWS(), Platform.getWS())) {
					matchInfos.add(new NameVersionDescriptor(plugin.getId(), plugin.getVersion()));
				}
			}

			// Because we used the directory container to get our bundles, we need to replace their parent
			for (int i = 0; i < bundles.length; i++) {
				bundles[i].setParentContainer(this);
			}
			return TargetDefinition.getMatchingBundles(bundles, (NameVersionDescriptor[]) matchInfos.toArray(new NameVersionDescriptor[matchInfos.size()]), null, this);
		} finally {
			if (model != null) {
				model.dispose();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.core.target.AbstractBundleContainer#resolveFeatures(org.eclipse.pde.internal.core.target.provisional.ITargetDefinition, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IFeatureModel[] resolveFeatures(ITargetDefinition definition, IProgressMonitor monitor) throws CoreException {
		// TODO Should match up with process in resolveBundles()
		if (definition instanceof TargetDefinition) {
			IFeatureModel[] allFeatures = ((TargetDefinition) definition).getFeatureModels(getLocation(false), monitor);
			for (int i = 0; i < allFeatures.length; i++) {
				if (allFeatures[i].getFeature().getId().equals(fId)) {
					if (fVersion == null || allFeatures[i].getFeature().getVersion().equals(fVersion)) {
						return new IFeatureModel[] {allFeatures[i]};
					}
				}
			}
		}
		return new IFeatureModel[0];
	}

	/**
	 * Returns whether the given target environment setting matches that of a fragments.
	 * 
	 * @param targetValue value in target definition
	 * @param fragmentValue value in fragment
	 * @param runningValue value of current running platform
	 * @return whether the fragment should be considered
	 */
	private boolean isMatch(String targetValue, String fragmentValue, String runningValue) {
		if (fragmentValue == null) {
			// unspecified, so it is a match
			return true;
		}
		if (targetValue == null) {
			return runningValue.equals(fragmentValue);
		}
		return targetValue.equals(fragmentValue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.core.target.impl.AbstractBundleContainer#isContentEqual(org.eclipse.pde.internal.core.target.impl.AbstractBundleContainer)
	 */
	public boolean isContentEqual(AbstractBundleContainer container) {
		if (container instanceof FeatureBundleContainer) {
			FeatureBundleContainer fbc = (FeatureBundleContainer) container;
			return fHome.equals(fbc.fHome) && fId.equals(fbc.fId) && isNullOrEqual(fVersion, fVersion);
		}
		return false;
	}

	private boolean isNullOrEqual(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}
		if (o2 == null) {
			return false;
		}
		return o1.equals(o2);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new StringBuffer().append("Feature ").append(fId).append(' ').append(fVersion).append(' ').append(fHome).toString(); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.core.target.provisional.IBundleContainer#getVMArguments()
	 */
	public String[] getVMArguments() {
		return null;
	}
}
