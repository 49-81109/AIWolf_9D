package org.aiwolf.ui.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.aiwolf.common.data.Player;

/**
 * Read Agent Library from File
 * @author tori
 *
 */
public class AgentLibraryReader {

	/**
	 * Find all jar files from target directory (or file)
	 * @param target
	 * @return
	 */
	static public List<File> getJarFileList(File target){
		List<File> jarFileList = new ArrayList<File>();
		if(target.isDirectory()){
			for(File file:target.listFiles()){
				jarFileList.addAll(getJarFileList(file));
			}
		}
		else if(target.getName().endsWith(".jar")){
			jarFileList.add(target);
		}
		return jarFileList;
	}
	
	
	/**
	 * Find player class from jar file
	 * @param jarFile
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings("rawtypes")
	static public List<Class> getPlayerClassList(File libraryFile) throws IOException{
		URL url = libraryFile.toURI().toURL();
		URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{url}, ClassLoader.getSystemClassLoader());
		
		List<Class> classList = new ArrayList<>();
//		System.out.println(url);
		JarFile jarFile;
		try {
			jarFile = new JarFile(Paths.get(url.toURI()).toFile());
			Enumeration<JarEntry> en = jarFile.entries();
			while (en.hasMoreElements()) {
				JarEntry entry = en.nextElement();
				if (entry.getName().endsWith("class")) {
					try {
						// System.out.println(entry.getComment());
						Class cls = Class.forName(entry.toString().replaceAll("/", ".").replaceAll("\\.class$", ""), true, classLoader);
						// System.out.println(cls);
						if (Player.class.isAssignableFrom(cls)) {
							if ((cls.getModifiers() & Modifier.ABSTRACT) == 0) {
								try {
									Object p = cls.newInstance();
									classList.add(cls);
								} catch (InstantiationException e) {
								} catch (IllegalAccessException e) {
								} catch (Exception e) {
								}
							}
						}
					} catch (ClassNotFoundException e) {
						// e.printStackTrace();
					} catch (NoClassDefFoundError e) {
						// e.printStackTrace();
					} catch (IllegalAccessError e) {
						// e.printStackTrace();
					}
				}
			}
			jarFile.close();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		return classList;
	}
	
}
