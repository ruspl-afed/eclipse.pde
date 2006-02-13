/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.build.ant;

import java.io.*;
import java.util.*;
import org.eclipse.pde.build.IAntScript;

/**
 * Class for producing Ant scripts. Contains convenience methods for creating the
 * XML elements required for Ant scripts. See the <a href="http://jakarta.apache.org/ant">Ant</a> 
 * website for more details on Ant scripts and the particular Ant tasks.
 */
public class AntScript implements IAntScript {

	protected OutputStream out;
	protected PrintWriter output;
	protected final String XML_PROLOG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; //$NON-NLS-1$
	protected int indent = 0;

	/**
	 * Constructor for the class.
	 * 
	 * @param out the output stream to write the script to
	 * @throws IOException
	 */
	public AntScript(OutputStream out) throws IOException {
		this.out = out;
		output = new PrintWriter(new OutputStreamWriter(out, "UTF8")); //$NON-NLS-1$
		output.println(XML_PROLOG);
	}

	/**
	 * Close the output stream.
	 */
	public void close() {
		output.flush();
		output.close();

		// introduced because sometimes the file was not closed.
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Print an <code>antcall</code> task to the script. This calls Ant on the given 
	 * target which is located within the same build file. 
	 * 
	 * @param target the target of the ant call
	 * @param inheritAll <code>true</code> if the parameters should be pass to the
	 * 	called target
	 * @param params table of parameters for the call
	 */
	public void printAntCallTask(String target, boolean inheritAll, Map params) {
		printTab();
		output.print("<antcall"); //$NON-NLS-1$
		printAttribute("target", target, true); //$NON-NLS-1$
		if (inheritAll == false)
			printAttribute("inheritAll", "false", false); //$NON-NLS-1$ //$NON-NLS-2$
		if (params == null)
			output.println("/>"); //$NON-NLS-1$
		else {
			output.println(">"); //$NON-NLS-1$
			indent++;
			Set entries = params.entrySet();
			for (Iterator iter = entries.iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry) iter.next();
				printParam((String) entry.getKey(), (String) entry.getValue());
			}
			indent--;
			printTab();
			output.println("</antcall>"); //$NON-NLS-1$
		}
	}

	/**
	 * Print a <code>jar</code> Ant task to this script. This jars together a group of 
	 * files into a single file.
	 * 
	 * @param jarFile the destination file name
	 * @param basedir the base directory
	 */
	public void printJarTask(String jarFile, String basedir, String manifestAttribute) {
		printTab();
		output.print("<jar"); //$NON-NLS-1$
		printAttribute("destfile", jarFile, true); //$NON-NLS-1$
		printAttribute("basedir", basedir, false); //$NON-NLS-1$
		printAttribute("manifest", manifestAttribute, false); //$NON-NLS-1$
		output.println("/>"); //$NON-NLS-1$
	}

	/**
	 * Print the <code>available</code> Ant task to this script. This task sets a property
	 * value if the given file exists at runtime.
	 * 
	 * @param property the property to set
	 * @param file the file to look for
	 */
	public void printAvailableTask(String property, String file) {
		printTab();
		output.print("<available"); //$NON-NLS-1$
		printAttribute("property", property, true); //$NON-NLS-1$
		printAttribute("file", file, false); //$NON-NLS-1$
		output.println("/>"); //$NON-NLS-1$
	}

	/**
	 * Print the <code>available</code> Ant task to this script. This task sets a property
	 * to the given value if the given file exists at runtime.
	 * 
	 * @param property the property to set
	 * @param file the file to look for
	 */
	public void printAvailableTask(String property, String file, String value){
		printTab();
		output.print("<available"); //$NON-NLS-1$
		printAttribute("property", property, true); //$NON-NLS-1$
		printAttribute("file", file, false); //$NON-NLS-1$
		printAttribute("value", value, false); //$NON-NLS-1$
		output.println("/>"); //$NON-NLS-1$
	}
	
	/**
	 * Print an <code>ant</code> task to this script. This calls Ant on the specified 
	 * target contained in the specified Ant file with the given parameters.
	 * 
	 * @param antfile the name of the Ant file which contains the target to run
	 * @param dir the basedir for the target
	 * @param target the name of the target
	 * @param outputParam filename to write the output to
	 * @param inheritAll <code>true</code> if the parameters should be passed on
	 * 	to the ant target
	 * @param properties the table of properties
	 */
	public void printAntTask(String antfile, String dir, String target, String outputParam, String inheritAll, Map properties ) {
		printAntTask(antfile, dir, target, outputParam, inheritAll, properties, null );
	}
	
	/**
	 * Print an <code>ant</code> task to this script. This calls Ant on the specified 
	 * target contained in the specified Ant file with the given parameters.
	 * 
	 * @param antfile the name of the Ant file which contains the target to run
	 * @param dir the basedir for the target
	 * @param target the name of the target
	 * @param outputParam filename to write the output to
	 * @param inheritAll <code>true</code> if the parameters should be passed on
	 * 	to the ant target
	 * @param properties the table of properties
	 * @param references the table of references
	 */
	public void printAntTask(String antfile, String dir, String target, String outputParam, String inheritAll, Map properties, Map references) {
		printTab();
		output.print("<ant"); //$NON-NLS-1$
		printAttribute("antfile", antfile, false); //$NON-NLS-1$
		printAttribute("dir", dir, false); //$NON-NLS-1$
		printAttribute("target", target, false); //$NON-NLS-1$
		printAttribute("output", outputParam, false); //$NON-NLS-1$
		printAttribute("inheritAll", inheritAll, false); //$NON-NLS-1$
		if (properties == null && references == null)
			output.println("/>"); //$NON-NLS-1$
		else {
			output.println(">"); //$NON-NLS-1$
			indent++;
			if( properties != null ) {
				Set entries = properties.entrySet();
				for (Iterator iter = entries.iterator(); iter.hasNext();) {
					Map.Entry entry = (Map.Entry) iter.next();
					printProperty((String) entry.getKey(), (String) entry.getValue());
				}
			}
			if( references != null ){
				Set entries = references.entrySet();
				for (Iterator iter = entries.iterator(); iter.hasNext();) {
					Map.Entry entry = (Map.Entry) iter.next();
					printTab();
					print("<reference refid=\"" + (String)entry.getKey() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					if( entry.getValue() != null ){
						print(" torefid=\"" + (String) entry.getValue() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					}
					print("/>"); //$NON-NLS-1$
					println();
				}
			}
			indent--;
			printTab();
			output.println("</ant>"); //$NON-NLS-1$
		}
	}

	public void printSubantTask(String antfile, String target, String buildpath, String failOnError, Map properties, Map references) {
		printTab();
		output.print("<subant"); //$NON-NLS-1$
		printAttribute("antfile", antfile, false); //$NON-NLS-1$
		printAttribute("target", target, false); //$NON-NLS-1$
		printAttribute("failonerror", failOnError, false);   //$NON-NLS-1$
		printAttribute("buildpath", buildpath, false);  //$NON-NLS-1$
		if (properties == null && references == null)
			output.println("/>"); //$NON-NLS-1$
		else {
			output.println(">"); //$NON-NLS-1$
			indent++;
			if( properties != null ) {
				Set entries = properties.entrySet();
				for (Iterator iter = entries.iterator(); iter.hasNext();) {
					Map.Entry entry = (Map.Entry) iter.next();
					printProperty((String) entry.getKey(), (String) entry.getValue());
				}
			}
			if( references != null ){
				Set entries = references.entrySet();
				for (Iterator iter = entries.iterator(); iter.hasNext();) {
					Map.Entry entry = (Map.Entry) iter.next();
					printTab();
					print("<reference refid=\"" + (String)entry.getKey() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					if( entry.getValue() != null ){
						print(" torefid=\"" + (String) entry.getValue() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
					}
					print("/>"); //$NON-NLS-1$
					println();
				}
			}
			indent--;
			printTab();
			output.println("</subant>"); //$NON-NLS-1$
		}
	}
	/**
	 * Print a <code>zip</code> task to this script.
	 * 
	 * @param zipfile the destination file name
	 * @param basedir the source directory to start the zip
	 * @param filesOnly <code>true</code> if the resulting zip file should contain only files and not directories
	 * @param update ndicates whether to update or overwrite the destination file if it already exists
	 * @param fileSets the inclusion/exclusion rules to use when zipping
	 */
	public void printZipTask(String zipfile, String basedir, boolean filesOnly, boolean update, FileSet[] fileSets) {
		printTab();
		output.print("<zip"); //$NON-NLS-1$
		printAttribute("destfile", zipfile, true); //$NON-NLS-1$
		printAttribute("basedir", basedir, false); //$NON-NLS-1$
		printAttribute("filesonly", filesOnly ? "true" : "false", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		printAttribute("whenempty", "skip", true); //$NON-NLS-1$//$NON-NLS-2$
		printAttribute("update", update ? "true" : "false", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (fileSets == null)
			output.println("/>"); //$NON-NLS-1$
		else {
			output.println(">"); //$NON-NLS-1$
			indent++;
			for (int i = 0; i < fileSets.length; i++)
				if (fileSets[i] != null)
					fileSets[i].print(this);
			indent--;
			printTab();
			output.println("</zip>"); //$NON-NLS-1$
		}
	}

	public void printTarTask(String zipfile, String basedir, boolean filesOnly, boolean update, FileSet[] fileSets) {
		printTab();
		output.print("<tar"); //$NON-NLS-1$
		printAttribute("destfile", zipfile, true); //$NON-NLS-1$
		printAttribute("basedir", basedir, false); //$NON-NLS-1$
		printAttribute("compression", "gzip", true); //$NON-NLS-1$//$NON-NLS-2$
		if (fileSets == null)
			output.println("/>"); //$NON-NLS-1$
		else {
			output.println(">"); //$NON-NLS-1$
			indent++;
			for (int i = 0; i < fileSets.length; i++)
				if (fileSets[i] != null)
					fileSets[i].print(this);
			indent--;
			printTab();
			output.println("</tar>"); //$NON-NLS-1$
		}
	}

	/**
	 * Print an <code>arg</code> element to the Ant file.
	 * 
	 * @param line
	 */
	protected void printArg(String line) {
		printTab();
		output.print("<arg"); //$NON-NLS-1$
		printAttribute("line", line, false); //$NON-NLS-1$
		output.println("/>"); //$NON-NLS-1$
	}

	/**
	 * Print the given string to the Ant script.
	 * 
	 * @param string the string to write to the file
	 */
	public void printString(String string) {
		printTab();
		output.println(string);
	}

	/**
	 * Print the given comment to the Ant script.
	 * 
	 * @param comment the comment to write out
	 */
	public void printComment(String comment) {
		printTab();
		output.print("<!-- "); //$NON-NLS-1$
		output.print(comment);
		output.println(" -->"); //$NON-NLS-1$
	}

	/**
	 * Add the given name/value attribute pair to the script. Do not write the attribute
	 * if the value is <code>null</code> unless a <code>true</code> is specified
	 * indicating that it is mandatory.
	 * 
	 * @param name the name of the attribute
	 * @param value the value of the attribute or <code>null</code>
	 * @param mandatory <code>true</code> if the attribute should be printed even
	 *   if it is <code>null</code>
	 */
	public void printAttribute(String name, String value, boolean mandatory) {
		if (mandatory && value == null)
			value = ""; //$NON-NLS-1$
		if (value != null) {
			output.print(" "); //$NON-NLS-1$
			output.print(name);
			output.print("="); //$NON-NLS-1$
			printQuotes(value);
		}
	}

	/**
	 * Print a <code>copy</code> task to the script. The source file is specified 
	 * by the <code>file</code> parameter. The destination directory is specified by 
	 * the <code>todir</code> parameter. 
	 * @param file the source file
	 * @param todir the destination directory
	 * @param fileSets the inclusion/exclusion rules to use when copying
	 * @param overwrite TODO
	 */
	public void printCopyTask(String file, String todir, FileSet[] fileSets, boolean failOnError, boolean overwrite) {
		printTab();
		output.print("<copy"); //$NON-NLS-1$
		printAttribute("file", file, false); //$NON-NLS-1$
		printAttribute("todir", todir, false); //$NON-NLS-1$
		printAttribute("failonerror", failOnError ? "true" : "false", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		printAttribute("overwrite", overwrite ? "true" : "false", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (fileSets == null)
			output.println("/>"); //$NON-NLS-1$
		else {
			output.println(">"); //$NON-NLS-1$
			indent++;
			for (int i = 0; i < fileSets.length; i++)
				fileSets[i].print(this);
			indent--;
			printTab();
			output.println("</copy>"); //$NON-NLS-1$
		}
	}

	public void printMoveTask(String todir, FileSet[] fileSets, boolean failOnError) {
		printTab();
		output.print("<move"); //$NON-NLS-1$
		printAttribute("todir", todir, false); //$NON-NLS-1$
		printAttribute("failonerror", failOnError ? "true" : "false", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		output.println(">"); //$NON-NLS-1$
		indent++;
		for (int i = 0; i < fileSets.length; i++)
			fileSets[i].print(this);
		indent--;
		printTab();
		output.println("</move>"); //$NON-NLS-1$
	}

	/**
	 * Print a <code>copy</code> tak to the script. The source file is specified by
	 * the <code>file</code> parameter.  The destination file is specified by the
	 * <code>toFile</code> parameter.
	 * @param file the source file
	 * @param toFile the destination file 
	 */
	public void printCopyFileTask(String file, String toFile, boolean overwrite) {
		printTab();
		output.print("<copy"); //$NON-NLS-1$
		printAttribute("file", file, false); //$NON-NLS-1$
		printAttribute("tofile", toFile, false); //$NON-NLS-1$
		printAttribute("overwrite", overwrite ? "true" : null, false); //$NON-NLS-1$ //$NON-NLS-2$
		output.println("/>"); //$NON-NLS-1$
	}

	/**
	 * Print a <code>delete</code> task to the Ant script. At least one of <code>dir</code>
	 * or <code>file</code> is required unless some <code>fileSets</code> are
	 * present.
	 * 
	 * @param dir the name of the directory to delete
	 * @param file the name of the file to delete
	 * @param fileSets the specification for the files to delete
	 */
	public void printDeleteTask(String dir, String file, FileSet[] fileSets) {
		printTab();
		output.print("<delete"); //$NON-NLS-1$
		printAttribute("dir", dir, false); //$NON-NLS-1$
		printAttribute("file", file, false); //$NON-NLS-1$
		if (fileSets == null)
			output.println("/>"); //$NON-NLS-1$
		else {
			output.println(">"); //$NON-NLS-1$
			indent++;
			for (int i = 0; i < fileSets.length; i++)
				fileSets[i].print(this);
			indent--;
			printTab();
			output.println("</delete>"); //$NON-NLS-1$
		}
	}

	/**
	 * Print an <code>exec</code> task to the Ant script.
	 * 
	 * @param executable the program to execute
	 * @param dir the working directory for the executable
	 * @param lineArgs the arguments for the executable
	 */
	public void printExecTask(String executable, String dir, List lineArgs, String os) {
		printTab();
		output.print("<exec"); //$NON-NLS-1$
		printAttribute("executable", executable, true); //$NON-NLS-1$
		printAttribute("dir", dir, false); //$NON-NLS-1$
		printAttribute("os", os, false); //$NON-NLS-1$
		if (lineArgs == null || lineArgs.size() == 0)
			output.println("/>"); //$NON-NLS-1$
		else {
			output.println(">"); //$NON-NLS-1$
			indent++;
			for (int i = 0; i < lineArgs.size(); i++)
				printArg((String) lineArgs.get(i));
			indent--;
			printTab();
			output.println("</exec>"); //$NON-NLS-1$
		}
	}

	/**
	 * Print a <code>mkdir</code> task to the Ant script.
	 * 
	 * @param dir the name of the directory to create.
	 */
	public void printMkdirTask(String dir) {
		printTab();
		output.print("<mkdir"); //$NON-NLS-1$
		printAttribute("dir", dir, false); //$NON-NLS-1$
		output.println("/>"); //$NON-NLS-1$
	}

	/**
	 * Print a <code>brand</code> task to the Ant script.
	 * 
	 * @param root the location of the launcher to brand.
	 * @param icons the list of icons to use in the branding
	 * @param name the name of the resultant launcher
	 */
	public void printBrandTask(String root, String icons, String name, String os) {
		printTab();
		print("<eclipse.brand"); //$NON-NLS-1$
		printAttribute("root", root, true); //$NON-NLS-1$
		if (icons != null)
			printAttribute("icons", icons, true); //$NON-NLS-1$
		printAttribute("name", name, true); //$NON-NLS-1$
		printAttribute("os", os, true); //$NON-NLS-1$
		println("/>"); //$NON-NLS-1$
	}

	/**
	 * Print an <code>echo</code> task to the Ant script.
	 * 
	 * @param message the message to echo to the output
	 */
	public void printEchoTask(String message) {
		printTab();
		output.print("<echo"); //$NON-NLS-1$
		printAttribute("message", message, true); //$NON-NLS-1$
		output.println("/>"); //$NON-NLS-1$
	}

	/**
	 * Print a <code>path</code> structure to the Ant Script.
	 * The list of paths are printed using path.toString(), so paths
	 * can be any Object.  Commonly String or ClasspathComputer3_0.ClasspathElement
	 * @param tag   - tag for the structure, normally path or classpath 
	 * @param id    - id for this structure
	 * @param paths - list of paths. Paths are printed using path.toString()
	 */
	public void printPathStructure(String tag, String id, List paths) {
		printTab();
		print("<" + tag); //$NON-NLS-1$
		if (id != null)
			print(" id=\"" + id + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		print(">"); //$NON-NLS-1$
		println();

		if (paths != null) {
			indent++;
			for (Iterator iter = paths.iterator(); iter.hasNext();) {
				Object path = iter.next();
				printTab();
				print("<pathelement"); //$NON-NLS-1$
				printAttribute("path", path.toString(), false); //$NON-NLS-1$
				print("/>"); //$NON-NLS-1$
				println();
			}
			indent--;
		}
		printEndTag(tag);
	}
	
	/**
	 * Print a <code>param</code> tag to the Ant script.
	 * 
	 * @param name the parameter name
	 * @param value the parameter value
	 */
	protected void printParam(String name, String value) {
		printTab();
		output.print("<param"); //$NON-NLS-1$
		printAttribute("name", name, true); //$NON-NLS-1$
		printAttribute("value", value, true); //$NON-NLS-1$
		output.println("/>"); //$NON-NLS-1$
	}

	/**
	 * Print a <code>project</code> tag to the Ant script.
	 * 
	 * @param name the name of the project
	 * @param target the name of default target
	 * @param basedir the base directory for all the project's path calculations
	 */
	public void printProjectDeclaration(String name, String target, String basedir) {
		output.print("<project"); //$NON-NLS-1$
		printAttribute("name", name, false); //$NON-NLS-1$
		printAttribute("default", target, true); //$NON-NLS-1$
		printAttribute("basedir", basedir, false); //$NON-NLS-1$
		output.println(">"); //$NON-NLS-1$
		indent++;
	}

	/**
	 * Print a <code>project</code> end tag to the Ant script.
	 */
	public void printProjectEnd() {
		indent--;
		printEndTag("project"); //$NON-NLS-1$
	}

	/**
	 * Print a <code>property</code> tag to the Ant script.
	 * 
	 * @param name the property name
	 * @param value the property value
	 */
	public void printProperty(String name, String value) {
		printTab();
		output.print("<property"); //$NON-NLS-1$
		printAttribute("name", name, true); //$NON-NLS-1$
		printAttribute("value", value, true); //$NON-NLS-1$
		output.println("/>"); //$NON-NLS-1$
	}

	public void printPropertyRefid(String name, String ref) {
		printTab();
		output.print("<property"); //$NON-NLS-1$
		printAttribute("name", name, true); //$NON-NLS-1$
		printAttribute("refid", ref, true); //$NON-NLS-1$
		output.println("/>"); //$NON-NLS-1$
	}

	/**
	 * Print the given string to the Ant script within quotes.
	 * 
	 * @param message the string to print
	 */
	protected void printQuotes(String message) {
		output.print("\""); //$NON-NLS-1$
		output.print(message);
		output.print("\""); //$NON-NLS-1$
	}

	/**
	 * Print a start tag in the Ant script for the given element name.
	 * 
	 * @param tag the name of the element
	 */
	public void printStartTag(String tag) {
		printTab();
		output.print("<"); //$NON-NLS-1$
		output.print(tag);
		output.println(">"); //$NON-NLS-1$
	}

	/**
	 * Print an end tag in the Ant script for the given element name.
	 * 
	 * @param tag the name of the element
	 */
	public void printEndTag(String tag) {
		printTab();
		output.print("</"); //$NON-NLS-1$
		output.print(tag);
		output.println(">"); //$NON-NLS-1$
	}

	/**
	 * Print the given number of tabs to the Ant script.
	 */
	public void printTab() {
		for (int i = 0; i < indent; i++)
			output.print("\t"); //$NON-NLS-1$
	}

	/**
	 * Print the given string to the Ant script followed by a carriage-return.
	 * 
	 * @param message the string to print
	 */
	public void println(String message) {
		printTab();
		output.println(message);
	}

	/**
	 * Print the given string to the Ant script.
	 * 
	 * @param message
	 */
	public void print(String message) {
		output.print(message);
	}

	/**
	 * Print a carriage-return to the Ant script.
	 */
	public void println() {
		output.println();
	}

	/**
	 * Print the given task to the Ant script.
	 * 
	 * @param task the task to print
	 */
	public void print(ITask task) {
		task.print(this);
	}

	/**
	 * Print a <code>target</code> tag to the Ant script.
	 * 
	 * @param name the name of the target
	 * @param depends a comma separated list of required targets
	 * @param ifClause the name of the property that this target depends on
	 * @param unlessClause the name of the property that this target cannot have
	 * @param description a user-readable description of this target
	 */
	public void printTargetDeclaration(String name, String depends, String ifClause, String unlessClause, String description) {
		printTab();
		output.print("<target"); //$NON-NLS-1$
		printAttribute("name", name, true); //$NON-NLS-1$
		printAttribute("depends", depends, false); //$NON-NLS-1$
		printAttribute("if", ifClause, false); //$NON-NLS-1$
		printAttribute("unless", unlessClause, false); //$NON-NLS-1$
		printAttribute("description", description, false); //$NON-NLS-1$
		output.println(">"); //$NON-NLS-1$
		indent++;
	}

	/**
	 * Print a closing <code>target</code> tag to the script. Indent the specified
	 * number of tabs.
	 */
	public void printTargetEnd() {
		indent--;
		printEndTag("target"); //$NON-NLS-1$
	}

	/**
	 * Print a <code>eclipse.refreshLocal</code> task to the script. This task refreshes
	 * the specified resource in the workspace, to the specified depth. 
	 * 
	 * @param resource the resource to refresh
	 * @param depth one of <code>IResource.DEPTH_ZERO</code>,
	 *   <code>IResource.DEPTH_ONE</code>, or <code>IResource.DEPTH_INFINITY</code>
	 */
	public void printRefreshLocalTask(String resource, String depth) {
		printTab();
		output.print("<eclipse.refreshLocal"); //$NON-NLS-1$
		printAttribute("resource", resource, true); //$NON-NLS-1$
		printAttribute("depth", depth, false); //$NON-NLS-1$
		output.println("/>"); //$NON-NLS-1$
	}

	public void printChmod(String dir, String rights, String files) {
		printTab();
		output.print("<chmod perm=\"" + rights + "\" "); //$NON-NLS-1$//$NON-NLS-2$
		output.print("dir=\"" + dir + "\" "); //$NON-NLS-1$//$NON-NLS-2$
		output.print("includes=\"" + files + "\" /> "); //$NON-NLS-1$ //$NON-NLS-2$
		output.println();
	}

	public void printGet(String source, String destination, String login, String password, boolean usetimestamp) {
		printTab();
		output.print("<get "); //$NON-NLS-1$
		printAttribute("username", login, false); //$NON-NLS-1$
		printAttribute("password", password, false); //$NON-NLS-1$
		printAttribute("src", source, true); //$NON-NLS-1$
		printAttribute("dest", destination, true); //$NON-NLS-1$
		printAttribute("usetimestamp", usetimestamp ? "true" : null, false); //$NON-NLS-1$ //$NON-NLS-2$
		output.println("/>"); //$NON-NLS-1$
	}

	public void printGZip(String source, String destination) {
		printTab();
		output.println("<gzip src=\"" + source + "\" zipfile=\"" + destination + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Print a <code> eclipse.convertTask</code> task to the script. This task convert a file path to 
	 * an Eclipse resource or vice-versa. 
	 *
	 * @param toConvert the entry to convert 
	 * @param propertyName the property where to store the result of the convertion
	 * @param isEclipseResource true if toConvert refers to an eclipse resource. 
	 */
	public void printConvertPathTask(String toConvert, String propertyName, boolean isEclipseResource) {
		printTab();
		output.print("<eclipse.convertPath"); //$NON-NLS-1$
		if (isEclipseResource == false)
			printAttribute("fileSystemPath", toConvert, true); //$NON-NLS-1$
		else
			printAttribute("resourcePath", toConvert, true); //$NON-NLS-1$
		printAttribute("property", propertyName, true); //$NON-NLS-1$
		output.println("/>"); //$NON-NLS-1$
	}

	/**
	 * Print a <code> dirname </code> task to the script.
	 * @param property
	 * @param file
	 */
	public void printDirName(String property, String file) {
		printTab();
		output.print("<dirname"); //$NON-NLS-1$
		printAttribute("property", property, true); //$NON-NLS-1$
		printAttribute("file", file, true); //$NON-NLS-1$
		output.println("/>"); //$NON-NLS-1$
	}
	
	/**
	 * Print a <code>Condition</code> task with isset test to the script
	 * @param property		name of the property to set	
	 * @param value			value to set the property to
	 * @param testProperty	name of the property for the isset test
	 */
	public void printConditionIsSet(String property, String value, String testProperty) {
		println("<condition property=\"" + property + "\" value=\"" + value + "\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		indent++;
		println("<isset property=\"" + testProperty + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
		indent--;
		printEndTag("condition"); //$NON-NLS-1$
	}
	
	public void printTabs() {
		printTab();
	}
}
