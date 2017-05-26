/*
 * Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 * 
 */

package org.jdesktop.jdic.init;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.jdesktop.jdic.browser.internal.WebBrowserUtil;
//import com.sun.jnlp.JNLPClassLoader; //bd
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;

/**
 * Initialization manager for JDIC to set the environment variables or
 * initialize the set up for native libraries and executable files.
 * <p>
 * There are 3 modes of operation: WebStart, file system, and .jar file.
 * <p>
 * When using WebStart, please specify a .jar file(jdic-native.jar) with the
 * native libraries for your platform to be loaded by WebStart in your JNPL.
 * This class will find the unjared native libraries and executables, and use
 * them directly.
 * <p>
 * If not in WebStart, the system will expect the native libraries to be located
 * in directory at the root of the classpath or .jar containing this class.
 * 
 * @author Michael Samblanet
 * @author Paul Huang
 * @author George Zhang
 * @author Michael Shan
 * @since July 29, 2004
 * @author Berthold Daum.
 * - Removed Web Start Facilities
 * - Adapted library loading to Eclipse RCP
 */
public class JdicManager {
    private static Class clNativeExtractor = null;        
    
    /** The path for the JDIC native files (jdic.dll/libjdic.so, etc) */
    static String nativeLibPath = null;


    /**
     * Private constructor to prevent public construction.
     */
    private JdicManager() {}

    /**
     * Returns the canonical name of the platform. This value is derived from the
     * System property os.name.
     * 
     * @return The platform string.
     */
    public static String getPlatform() {
        // See list of os names at: http://lopica.sourceforge.net/os.html
        // or at: http://www.tolstoy.com/samizdat/sysprops.html
        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) {
                return "windows";
        }
        return canonical(osname);
    }

    /**
     * Returns the default DLL extension for platform. 
     * 
     * @return The default DLL extension.
     */
    public static String getPlatformDLLext() 
    {
        if (getPlatform().equals("windows")) {
            return ".dll";
        }
        return ".so";
    }    

    /**
     * Returns the class name suffix for class loader. This value is derived from the
     * System property os.name.
     * 
     * @return The class suffix string.
     */
    public static String getPlatformSuffix()
    {
        // See list of os names at: http://lopica.sourceforge.net/os.html
        // or at: http://www.tolstoy.com/samizdat/sysprops.html
        String osname = System.getProperty("os.name");
        if (osname.startsWith("Windows")) {
            return "windows";
        } 
        return "unix";
    }            
    
    /**
     * Return the name of the architecture. This value is determined by the
     * System property os.arch.
     * 
     * @return The architecture string.
     */
    public static String getArchitecture() {
        String arch = System.getProperty("os.arch");
        if (arch.endsWith("86")) {
                return "x86";
        }
        return canonical(arch);
    }

    /**
     * @param value
     *            The value to be canonicalized.
     * @return The value with all '/', '\' and ' ' replaced with '_', and all
     *         uppercase characters replaced with lower case equivalents.
     */
    private static String canonical(String value) {
        WebBrowserUtil.trace("value:" + value);
        WebBrowserUtil.trace("canonical:"
                        + value.toLowerCase().replaceAll("[\\\\/ ]", "_"));
        return value.toLowerCase().replaceAll("[\\\\/ ]", "_");
    }
    
    /**
     * Initializes the shared native file settings for all the JDIC components/
     * packages. Set necessary environment variables for the shared native
     * library and executable files, including *.dll files on Windows, and *.so
     * files on Unix.
     * 
     * @exception JdicInitException Generic initialization exception
     */
    public static void init() throws JdicInitException 
    {
        try {
            WebBrowserUtil.trace("{Native loader");
            clNativeExtractor = NativeExtractor.class;
//            System.err.println("Native loader!");       // bd     
            WebBrowserUtil.trace("}Native loader");
        } catch (Throwable e) {
            //that is ok!
        }
        try {        
//            String jwsVersion = System.getProperty("javawebstart.version");  // bd
//            if( null != jwsVersion ){
//                WebBrowserUtil.trace("Loaded by JavaWebStart,version is " + jwsVersion);
//                if( null!=clNativeExtractor ){
//                    WebBrowserUtil.trace("plagin-2 in action!");
//                } else {
//                    ClassLoader cl = JdicManager.class.getClassLoader();
//                    if (cl instanceof JNLPClassLoader) {
//                        // Initialize native libs' running path if loaded by webstart.This method
//                        // only works for sun webstart implementaion,for other webstart
//                        // implementations, you have to rewrite this method.
//                        nativeLibPath = (new File(
//                            JNLPClassLoaderAccessor.findLibrary(
//                                (JNLPClassLoader) cl, 
//                                "jdic")
//                        )).getParentFile().getCanonicalPath();
//                    } else {
//                        // only run well for sun jre
//                        throw new JdicInitException(
//                            "Unexpected ClassLoader for webstart, only com.sun.jnlp.JNLPClassLoader is supported.");
//                    }
//                }
//            } else {
                // Find the root path of this class.
                if( null==clNativeExtractor 
                    && JdicManager.class.getClassLoader() instanceof URLClassLoader)
                {
                    //running url of current class                                                  
                    nativeLibPath =  new File(
                            new URL(JdicManager.class
                                .getProtectionDomain()
                                .getCodeSource()
                                .getLocation(),
                                ".")
                            .openConnection()
                            .getPermission()
                            .getName()
                        ).getCanonicalPath()
                        + File.separator + getPlatform() 
                        + File.separator + getArchitecture();
                }  
//            }    
            if(null!=clNativeExtractor){
                //we don't need adjust java.library.path,
                //but we have to add the path in initBrowserNative() call 
                nativeLibPath = NativeExtractor.getBinary();
            }
            
//        } catch( JdicInitException e ) {
//            throw e;
        } catch( Throwable e ) {
            throw new JdicInitException(e);
        }
    }    
    
    private static String getBinaryPath() {
        //WebBrowserUtil.trace("native lib path " + nativeLibPath);
        return nativeLibPath;
    }
    
    /**
     * Initializes the native file settings for the JDIC Browser component 
     * (package <code>org.jdecktop.jdic.browser</code>). Set necessary 
     * environment variables for the Browser specific native library and 
     * executable files, including *.exe files on Windows, and mozembed-<os>-gtk* 
     * files on Unix.
     * 
     * @exception JdicInitException Generic initialization exception
     */
    public static void initBrowserNative() throws JdicInitException {
        // The Browser component is used.
        // If the Browser specific native file setting was already initialized, 
        // just return.
        try {
            // Pre-append the JDIC binary path to PATH(on Windows) or 
            // LD_LIBRARY_PATH(on Unix).         
            
            /** The environment variable for library path setting */
            boolean isWindows = getPlatform().equals("windows");
            String libPathEnv = isWindows ? "PATH" : "LD_LIBRARY_PATH";
            
            String binaryPath = getBinaryPath();            
            InitUtility.preAppendEnv(libPathEnv, binaryPath); 
            WebBrowserUtil.trace("JDIC found BIN path=[" + binaryPath + "]");

            String browserPath = WebBrowserUtil.getBrowserPath();
            if (browserPath == null) {
                throw new JdicInitException(
                    "Can't locate the native browser path!");
            }
            
            if (WebBrowserUtil.isDefaultBrowserMozilla()) {
                // Mozilla is the default/embedded browser.
                // Use the user defined value or the mozilla binary
                // path as the value of MOZILLA_FIVE_HOME env variable.
                String envMFH = InitUtility.getEnv("MOZILLA_FIVE_HOME");
                if (envMFH == null) {
                    File browserFile = new File(browserPath);
                    if (browserFile.isDirectory()) {
                        envMFH = browserFile.getCanonicalPath();
                    } else {
                        envMFH = browserFile.getCanonicalFile().getParent();
                    }                    
                }
                
                if (!isWindows) {
                    // On Unix, add the binary path to PATH.
                    InitUtility.preAppendEnv("PATH", binaryPath);
                } else {               
                    // Mozilla on Windows, reset MOZILLA_FIVE_HOME to the GRE 
                    // directory path:  
                    //   [Common Files]\mozilla.org\GRE\1.x_BUILDID, 
                    // if Mozilla installs from a .exe package.
                    //                
                    String xpcomPath = envMFH + File.separator + "xpcom.dll";                        
                    if (!(new File(xpcomPath).isFile())) {
                        // Mozilla installs from a .exe package. Check the 
                        // installed GRE directory.
                        String mozGreHome 
                            = WebBrowserUtil.getMozillaGreHome();
                        if (mozGreHome == null) {
                            throw new JdicInitException(
                                "Can't locate the GRE directory of the " +
                                "installed Mozilla binary: " + envMFH);
                        }                       
                        envMFH = mozGreHome;
                    }
                }              

                InitUtility.setEnv("MOZILLA_FIVE_HOME", envMFH);
                InitUtility.preAppendEnv(libPathEnv, envMFH);
            } // end - Mozilla is the default/embedded browser.
        } catch (Throwable e) {
            throw new JdicInitException(e);
        }
    }

    private static boolean initNativeLoader = false;        
    private static HashSet loadedLibraries = new HashSet();  
    
    public static synchronized void loadLibrary(final String libName) 
            throws PrivilegedActionException 
    {
    	if( !loadedLibraries.contains(libName) ){  // bd
            loadedLibraries.add(libName);
      	    Library.loadLibrary(libName, true);
      	}
//        try {        //bd
//            if(!initNativeLoader){
//                initNativeLoader = true;
//                init();
//            }
//            if( !loadedLibraries.contains(libName) ){
//                loadedLibraries.add(libName);
//                if(null!=clNativeExtractor){
//                    NativeExtractor.loadLibruary(libName);                    
//                } else {
//                    AccessController.doPrivileged( new PrivilegedExceptionAction() { 
//                        public Object run() throws IOException {
//                            System.load(nativeLibPath + File.separator 
//                                    + libName + getPlatformDLLext());
//                            return null;
//                        }
//                    });
//                }    
//            }
//        }catch(PrivilegedActionException e){
//            throw e;
//        }catch(Exception e){
//            throw new PrivilegedActionException(e);
//        }
    }
    
    private static boolean initBrowserDLLPath = false;        
    public static synchronized Process exec( final String[] args) 
            throws PrivilegedActionException 
    {
        try {        
            if(!initNativeLoader){
                initNativeLoader = true;
                init();
            }
            if(!initBrowserDLLPath){
                initBrowserDLLPath = true;
                initBrowserNative();
            }
            if(null!=clNativeExtractor){
                return NativeExtractor.exec(args);                    
            } else {
                final Process[] res = new Process[] {null};
                AccessController.doPrivileged( new PrivilegedExceptionAction() { 
                    public Object run() throws IOException {
                        res[0] = Runtime.getRuntime().exec( args );
                        return null;
                    }
                });
                return res[0];
            }    
        }catch(PrivilegedActionException e){
            throw e;
        }catch(Exception e){
            throw new PrivilegedActionException(e);
        }
    }        
}

//class JNLPClassLoaderAccessor {  //bd
//    static java.lang.reflect.Method mdJNLPClassLoader_findLibrary = null;
//    static{
//        java.security.AccessController.doPrivileged( new java.security.PrivilegedAction() {
//            public Object run() {
//                try {
//                    mdJNLPClassLoader_findLibrary = Class
//                            .forName("com.sun.jnlp.JNLPClassLoader")
//                            .getDeclaredMethod(
//                                "findLibrary", 
//                                new Class[]{String.class} );
//                    mdJNLPClassLoader_findLibrary.setAccessible(true);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                // to please javac
//                return null;
//            }
//        });
//    }
//
//    public static String findLibrary(JNLPClassLoader o, String name) { 
//        try {
//            return (String)mdJNLPClassLoader_findLibrary.invoke(o, new Object[]{ name });
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
