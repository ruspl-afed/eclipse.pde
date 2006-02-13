/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.build;

import java.util.Map;

/**
 * An interface providing helper methods to produce Ant scripts. 
 * <p> 
 * It contains convenience methods for creating the XML elements 
 * required for Ant scripts. See the <a href="http://ant.apache.org">Ant</a> 
 * website for more details on Ant scripts and the particular Ant tasks.
 * </p>
 */
public interface IAntScript {

	/**
	 * Print the given string to the Ant script.
	 * 
	 * @param string the string to print.
	 */
	public void print(String string);

	/**
	 * Print the given comment to the Ant script folled by a carriage-return.
	 * 
	 * @param comment the comment to print.
	 */
	public void printComment(String comment);

	/**
	 * Print the given string followed by a carriage-return.
	 * 
	 * @param string the string to print.
	 */
	public void println(String string);

	/**
	 * Print a empty line.
	 */
	public void println();
	
	/**
	 * Print an ant call task as defined on {@link http://ant.apache.org/manual/CoreTasks/antcall.html }.
	 * @param target the target executed by the call. This value can not be <code>null</code>.
	 * @param inheritAll If true, pass all properties to the new Ant project.
	 * @param params Specifies as key / value pairs, the properties to set before running the specified target. This value can be <code>null</code>
	 */
	public void printAntCallTask(String target, boolean inheritAll, Map params);
	
	/**
	 * Print an XML attribute. <code>name=value</code>.
	 * @param name the name of the attribute to print. This value can not be <code>null</code>.
	 * @param value the name of the value to print. This value can be <code>null</code>.
	 * @param mandatory indicate whether or not the value is mandatory. 
	 * If the <code>value</code> is <code>null</code> and the attribute is mandatory, the printed value will be "". 
	 */
	public void printAttribute(String name, String value, boolean mandatory);
	
	/**
	 * Print tagName as an xml begin tag (<code>&lt;tagName&gt;<code>).
	 * @param tagName the tag to print.
	 */
	public void printStartTag(String tagName);
	
	/**
	 * Print tagName as an xml end tag (<code>&lt;/tagName&gt;<code>).
	 * @param tagName the tag to print.
	 */
	public void printEndTag(String endTag);
	
	/**
	 * Print as many tabs as current nesting level requires
	 */
	public void printTabs();
	
	/**
	 * Print a target declaration. See {@link http://ant.apache.org/manual/using.html#targets }. 
	 * @param name the name of the target. This value can not be <code>null</code>.
	 * @param depends a comma-separated list of names of targets on which this target depends. This value can be <code>null</code>.
	 * @param ifClause the name of the property that must be set in order for this target to execute. This value can be <code>null</code>
	 * @param unlessClause the name of the property that must not be set in order for this target to execute. This value can be <code>null</code>
	 * @param description a short description of this target's function. This value can be <code>null</code>
	 */
	public void printTargetDeclaration(String name, String depends, String ifClause, String unlessClause, String description);
	
	/**
	 * Print the end tag for a target declaration.
	 */
	public void printTargetEnd();
}
