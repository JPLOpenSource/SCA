package gov.nasa.jpl.statechart;

import gov.nasa.jpl.statechart.template.FunctionCall;
import gov.nasa.jpl.statechart.uml.Element;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


public final class Util {
    private Util () {
        // Do not allow instantiation.
    }

    /** Default package name separator, "::". */
    public static final String PACKAGE_SEP = "::";

    /**
     * Returns whether error level message is on.
     * Currently always true... until we hook in a Logger!
     * @return
     */
    public static boolean isErrorLevel () {
        return true;
    }

    /**
     * Logs (currently to just stderr) error message if error level is on.
     * @param msg  messages string to print
     */
    public static void error (String msg) {
        if (isErrorLevel()) {
            System.err.println(msg);
        }
    }

    /**
     * Returns whether warning level message is on.
     * Currently just indicates whether Autocoder verbose output is on.
     * @return  <code>true</code> if log level is warning or above, <code>false</code> otherwise.
     */
    public static boolean isWarningLevel () {
        return Autocoder.isVerbose();
    }

    /**
     * Logs (currently to stderr) warning message if warning level on.
     * @param msg  message string to print to log.
     */
    public static void warn (String msg) {
        if (isWarningLevel()) {
            System.err.println(msg);
        }
    }

    public static boolean isInfoLevel () {
        return Autocoder.isVerbose();
    }
    public static void info (String msg) {
        if (isInfoLevel()) {
            System.out.println(msg);
        }
    }

    public static boolean isDebugLevel () {
        return Autocoder.isDebugOn();
    }
    public static void debug (String msg) {
        if (isDebugLevel()) {
            System.out.println(msg);
        }
    }

    public static void reportException (Throwable e, String...addition) {
        if (isDebugLevel()) {
            e.printStackTrace();
        } else {
            error(join(addition, "") + e.getLocalizedMessage());
        }
    }

    /**
     * Checks if the passed string conforms to method call syntax, e.g.,
     * "someFunction(e)".
     * @param str  the text to check.
     * @return <code>true</code> if the supplied string represents a method call;
     * <code>false</code> otherwise.
     * @deprecated Use {@link FunctionCall#isFunctionCall(String)} instead
     */
    public static boolean isFunctionCall (String str) {
        return FunctionCall.isFunctionCall(str);
    }

    /**
     * Wrapped funation to reduce the verbosity of generic instantiations
     */
    public static <T> List<T> newList () {
        return new ArrayList<T>();
    }

    public static <T> List<T> newList (Collection<? extends T> c) {
        return new ArrayList<T>(c);
    }

    public static <T> Set<T> newSet () {
        return new HashSet<T>();
    }

    public static <T> Set<T> newSet (Collection<? extends T> c) {
        return new HashSet<T>(c);
    }

    public static <T> SortedSet<T> newSortedSet () {
        return new TreeSet<T>();
    }

    public static <T> SortedSet<T> newSortedSet (Collection<? extends T> c) {
        return new TreeSet<T>(c);
    }

    public static <K, V> Map<K, V> newMap () {
        return new HashMap<K, V>();
    }

    public static <K, V> Map<K, V> newMap (Map<? extends K, ? extends V> m) {
        return new HashMap<K, V>(m);
    }

    public static <K, V> SortedMap<K, V> newSortedMap () {
        return new TreeMap<K, V>();
    }

    public static <K, V> SortedMap<K, V> newSortedMap (
            Map<? extends K, ? extends V> m) {
        return new TreeMap<K, V>(m);
    }

    /**
     * Get the length of the longest string in a a collection
     */
    public static int getLongestString (Collection<String> list) {
        int max = 0;
        for (String s : list)
            max = Math.max(max, s.length());

        return max;
    }

    /**
     * Splits a given string by the given delimiter, but preserving quoted
     * strings in the process.  So, for example, if the delimiter is a comma,
     * then:<blockquote>
     * 1,"x,y" should be split into two arguments, not three
     * </blockquote>
     * N.B. this method uses regex, so it will NOT protect any other open/close
     * delimiters like (), [], etc.
     * 
     * @param str    the string to split
     * @param delim  the delimiter to split by
     * @return  a split Array of strings, with quoted strings preserved
     */
    public static String[] splitPreservingQuotes (String str, String delim) {
        List<String> argsFound = Util.newList();
        // regex pattern needs to account for commas/delim in quotes
        String delimTrim = "\\s*" + delim + "\\s*";
        String patternStr = "(\\s*" + delim + ")?\\s*"      /* leading delim */
                + "((\\\".*?\\\")|(\\\'.*?\\\'))"   /* single-/double-quotes */
                + "\\s*(" + delim + ")?";                  /* trailing delim */
        Pattern qp = Pattern.compile(patternStr);

        while (str != null && str.length() > 0) {
            /* Algorithm:  Look for a match with quoted-pattern.
             *   If found, look for any non-quoted args before it.
             *   Add those in location-order to list.
             *   If not found, split the remaining string by comma/delim.
             */
            Matcher m = qp.matcher(str);
            if (m.find()) {
                int foundIdx = m.start();
                if (foundIdx > 0) {
                    // split the string preceding the match by comma/delim,
                    // and allow trailing empty params
                    String nonMatched = str.substring(0, foundIdx);
                    argsFound.addAll(Arrays.asList(nonMatched.split(delimTrim,-1)));
                }
                // save the matched arg to list
                argsFound.add(m.group(2));
                str = str.substring(m.end());
            } else {  // no more quoted-pattern, just split based on comma/delim
                // and allow trailing empty params
                argsFound.addAll(Arrays.asList(str.trim().split(delimTrim,-1)));
                str = "";
            }
        }

        return argsFound.toArray(new String[0]);
    }
    /**
     * Join a collection with a separator string
     */
    public static String join (Collection<?> objs, String sep) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Object obj : objs) {
            if (!first) {
                sb.append(sep);
            }
            sb.append(obj.toString());
            first = false;
        }

        return sb.toString();
    }
    /**
     * Join an array with a separator string
     */
    public static <T> String join (T[] array, String sep) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (T e : array) {
            if (!first) {
                sb.append(sep);
            }
            sb.append(e.toString());
            first = false;
        }

        return sb.toString();
    }

    /**
     * Returns the name, joined to prefixes, if any, using the default
     * {@link #PACKAGE_SEP} if none supplied.
     * 
     * @param prefixes  String array of prefix labels.
     * @param name      Name to prefix with prefixes
     * @param sep       Separator, or <code>null</code> to use package separator.
     * @return  Concatenated string of prefixes and name.
     */
    public static String joinWithPrefixes (String[] prefixes, String name, String sep) {
        if (isVarArgsEmpty(prefixes)) {
            return name;
        } else {  // return named join to prefix
            if (sep == null) {
                sep = PACKAGE_SEP;
            }
            return join(prefixes, sep) + sep + name;
        }
    }

    /**
     * Returns whether the supplied var args is empty, meaning <code>null</code>,
     * or zero-length, or has a single element of length zero!
     * 
     * @param varArgs  variable argument to check.
     * @return  <code>true</code> if var args is considered empty, <code>false</code> otherwise.
     */
    public static boolean isVarArgsEmpty (String... varArgs) {
        boolean rv = false;
        if (varArgs == null || varArgs.length == 0
                || (varArgs.length == 1
                        && (varArgs[0] == null || varArgs[0].length() == 0))) {
            rv = true;
        }
        return rv;
    }

    /**
     * Returns whether the given string is one entirely enclosed within quotes,
     * e.g., "str" or 'str'.
     * 
     * @param testStr  the string to check if quoted
     * @return  <code>true</code> if string is of form "str" or 'str';
     *     <code>false</code> otherwise, such as mixed quotes.
     */
    public static boolean isQuotedString (String testStr) {
        boolean rv = false;

        if (testStr != null) {
            testStr = testStr.trim();
            if (testStr.length() > 0) {
                char firstChar = testStr.charAt(0);
                char lastChar = testStr.charAt(testStr.length()-1);
                if ((firstChar == '"'&& lastChar == '"')
                        || (firstChar == '\'' && lastChar == '\'')) {
                    rv = true;
                }
            }
        }

        return rv;
    }

    /**
     * Returns whether the given string is a valid real number,
     * checkable by parsing as a Double. 
     * 
     * @param testStr  the string to check if a number
     * @return  <code>true</code> if string represents a number; <code>false</code> otherwise.
     */
    public static boolean isNumber (String testStr) {
        try {  // see if string is a double
            Double.parseDouble(testStr);
            return true;  // no format exception
        } catch (NumberFormatException e) {
        }
        return false;
    }
    /**
     * Returns whether the given string is a literal or not.
     * We distinguish basically two kinds of literals, a number or a string.
     * Anything else is treated as a potential identifier.
     * 
     * @param str  string to test whether a literal or not
     * @return  <code>true</code> if string represents a literal value, <code>false</code> otherwise.
     * @see Util#isQuotedString(String)
     * @see Util#isNumber(String)
     */
    public static boolean isLiteral (String str) {
        // Use short-circuit eval to try one of these
        return Util.isNumber(str) || Util.isQuotedString(str);
        // Otherwise, NOT a literal we recognize
    }

    /**
     * Force the first character to be lower case.  If there are spaces in
     * the string, remove them and replace the next character with a
     * capitalized version of itself.
     */
    public static String toCamelCase (String s) {
        String str = s.trim();
        StringBuilder buffer = new StringBuilder();
        boolean capitalizeNext = true;  // signal to capitalize next char

        for (int i = 0; i < str.length(); i++) {
            // If there is a none-alphanumeric character, skip it and insert
            // the next character capitalized
            if (!Character.isLetterOrDigit(str.charAt(i))) {
                capitalizeNext = true;
                continue;
            }

            if (capitalizeNext) {  // will capitalize the first character
                buffer.append(Character.toUpperCase(str.charAt(i)));
                capitalizeNext = false;
                continue;
            }

            // Otherwise, just copy
            buffer.append(str.charAt(i));
        }

        return buffer.toString();
    }

    /**
     * Return a string made up of a single character repeated n times.
     */
    public static String strrep (char c, int n) {
        char[] s = new char[n];
        for (int i = 0; i < n; i++)
            s[i] = c;

        return new String(s);
    }

    /**
     * Converts a base-ten integer to a hexadecimal value at a particular digit.
     * @param value  base-10 integer to convert
     * @param digitPosition  designated hex digit position
     * @return  String representation of the hex digit
     */
    public static String toHexDigit (int value, int digitPosition) {
        int digitValue = (value >>> (digitPosition * 4)) & 0xf;

        return Integer.toHexString(digitValue);
    }

    /**
     * Returns a base-10 integer as a hexadecimal number in String.
     * @param value  base-10 integer to convert
     * @param numDigits  number of hex digits to display
     * @return  String representation of the hex number
     */
    public static String toHexString (int value, int numDigits) {
        StringBuilder result = new StringBuilder();
        for (int i = (numDigits - 1); i >= 0; i--) {
            String digit = toHexDigit(value, i);
            result.append(digit);
        }
        return result.toString().toUpperCase();
    }

    /**
     * Returns a name prefixed by <code>prefix</code>, joined with ':'.
     * 
     * @param prefix  prefix string to add to <code>name</code>
     * @param name    name string to prefix
     * @return <code>prefix</code>:<code>name</code>
     */
    public static String prefixed (String prefix, String name) {
        return prefix + ":" + name;
    }


    /**
     * A convenience function that takes a Collection of UML Elements
     * and filters the list by class type
     *
     * @param <T> Class type of list elements.
     * @param elements  List of elements.
     * @param clazz  Class type to filter.
     * @return  New List of elements filtered by <code>clazz</code>.
     */
    public static <T> List<T> filter (Collection<? extends Element> elements, java.lang.Class<T> clazz) {
        List<T> filteredList = newList();
    
        for (Element element : elements) {
            if (clazz.isAssignableFrom(element.getClass())) {
                filteredList.add(clazz.cast(element));
            }
        }
    
        return filteredList;
    }

    /**
     * A convenience function that takes a Collection of UML Elements of
     * uniform type, and filters OUT items of the specified subclass type.
     *
     * @param <T> Class type of list elements.
     * @param elements  List of elements.
     * @param clazz  Class type of subtype elements to eliminate,
     * @return  New List of elements with <code>clazz</code> subtypes removed.
     */
    public static <T,U extends T> List<T> filterOut (Collection<T> elements, java.lang.Class<U> clazz) {
        List<T> filteredList = newList();
    
        for (T element : elements) {
            if (!clazz.isAssignableFrom(element.getClass())) {
                filteredList.add(element);
            }
        }
    
        return filteredList;
    }

    /**
     * A convenience function that takes a Map of UML Elements and filters its
     * values by class type.
     * 
     * @param <K> Class type of Map key.
     * @param <V> Class type of Map value.
     * @param elements  Map of elements.
     * @param clazz  Class type to filter.
     * @return  New Map of elements whose values have been filtered by <code>clazz</code>.
     */
    public static <K, V> Map<K, V> filter (Map<K, ? extends Element> elements, java.lang.Class<V> clazz) {
        Map<K, V> filteredMap = newMap();
    
        for (Map.Entry<K, ? extends Element> entry : elements.entrySet()) {
            if (clazz.isAssignableFrom(entry.getValue().getClass())) {
                filteredMap.put(entry.getKey(), clazz.cast(entry.getValue()));
            }
        }

        return filteredMap;
    }


    /**
     * Given a class <code>clazz</code>, searches within system classpaths all
     * the classes at or under the path of <code>clazz</code>'s package, and
     * returns the subset that matches a given name pattern AND are <em>_leaf_</em>
     * subclasses ({@link Class#isAssignableFrom(Class)} of <code>clazz</code>.
     * <p>
     * This function searches within both regular directories and Jar files
     * listed on the classpath.  This function does assume that subclasses
     * appear under the directory structure of the package containing <code>clazz</code>.
     * </p>
     * @param clazz    class within whose package to search
     * @param pattern  regex name pattern to filter set, or null to match all;
     * the supplied pattern goes within {pattern} in
     * "(&lt;packagePath&gt;)?((.*<nobr>/</nobr>)?{pattern}.class)$"
     * @param leafOnly flag indicating whether to take leaf classes only
     * @return set of classes
     */
    public static <T> Set<Class<? extends T>> findSubclassesUnder (Class<? extends T> clazz, String pattern, boolean leafOnly) {
    	boolean internalDebug = false;

    	Set<Class<? extends T>> classSet = newSet();
        Set<String> candidates = new LinkedHashSet<String>();
        // search for classes under the directory identified by this package
        String packageName = clazz.getPackage().getName();
        // for this procedure, we need to make patterns with both '/' and '\' seps
        String sepPatterns = "[/\\\\]";
        String fullPattern = "(" + packageName.replace(".", sepPatterns)
                + sepPatterns + ")?((.*" + sepPatterns + ")?"
                + (pattern == null ? ".+": pattern) + ".class)$";
        if (internalDebug) System.out.println("Match pattern: " + fullPattern);
        final Pattern p = Pattern.compile(fullPattern);
        // collect the set of paths to search from classpath
        List<String> searchList = new ArrayList<String>();
        searchList.add(".");  // add current path in case it's a regular dir
        String userdir = System.getProperty("user.dir");
        if (internalDebug) System.out.println("Classpath: " + System.getProperty("java.class.path"));
        StringTokenizer tokens = new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator);
        while (tokens.hasMoreTokens()) {
            String path = tokens.nextToken();
            searchList.add(path.replaceAll("\\\\", "/"));
        }

        // now search all the paths
        for (String path : searchList) {
            if (path.equals(".")) {
                URL packageURL = clazz.getResource(".");
                if (packageURL != null) {
                    candidates.addAll(searchRegularDirectory(packageURL.getPath(), p));
                }
            } else {  // look at a classpath entry
                String abspath = null;  // make path absolute first
                if (new File(path).isAbsolute()) {  // already an absolute path
                    abspath = path;
                } else {  // make relative to user path
                    try {
                        abspath = new File(userdir + File.separator + path).getCanonicalPath().replaceAll("\\\\", "/");
                    } catch (IOException e) {
                        abspath = path;  // can't do much more
                    }
                }
                if (path.endsWith(".jar")) {  // look inside this jar for this class
                	// N.B.: Jar path MUST use '/' to be well-formed
                    String jarpath = String.format("jar:file:%s!/", abspath);
                    try {
                        JarURLConnection conn = (JarURLConnection)new URL(jarpath).openConnection();
                        JarFile jarfile = conn.getJarFile();
                        Set<String> matched = new HashSet<String>();
                        for (JarEntry je : Collections.list(jarfile.entries())) {
                            Matcher m = p.matcher(je.getName());
                            if (m.matches() && !matched.contains(m.group(2))) {
                                if (internalDebug) System.out.println(
                                		"Jar entry " + m.group()
                                		+ "; matcher: " + m.groupCount()
                                		+ ", 1->" + m.group(1) + ", 2->" + m.group(2));
                            	// N.B.: Jar entries use '/' as file separator
                                candidates.add(m.group(2).replace("/", "."));
                                matched.add(m.group(2));
                            }
                        }
                        matched.clear();
                    } catch (MalformedURLException e) {  // ignore error
                    } catch (IOException e) {  // ignore error
                    }
                } else {
                    candidates.addAll(searchRegularDirectory(abspath, p));
                }
            }
        }
        if (internalDebug) System.out.println("Matched candidate files: "
        		+ Arrays.toString(candidates.toArray()));

        // go through the list and find the properly annotated classes.
        for (String helperName : candidates) {
            int index = helperName.indexOf(".class");
            String className = packageName + "." + helperName.substring(0, index);
            try {
                // make sure class exists, exception if not
                Class<?> c = Class.forName(className);
                // check that class is a proper subclass
                if (clazz.isAssignableFrom(c)) {
                	boolean okToAdd = true;
                	if (leafOnly) {
                    	// then look for leaf class
                    	for (Class<?> addedC: newSet(classSet)) {
                    		Class<?> addedSuper = addedC.getSuperclass();
                    		Class<?> cSuper = c.getSuperclass();
                    		if (addedC.equals(cSuper)) {
                    			// c is a subclass, remove addedC
                    			classSet.remove(addedC);
                    		} else if (c.equals(addedSuper)) {
                    			// addedC already a subclass, so c should NOT be added to set
                    			if (internalDebug) {
                    				System.out.println("addedC: " + addedC.getCanonicalName()
                    					+ "; c: " + c.getCanonicalName()
                    					+ "; c.getSuperclass(): "
                    					+ (c.getSuperclass() == null ? null : cSuper.getCanonicalName()));
                    			}
                    			okToAdd = false;
                    		}
                    	}
                	}
                	if (okToAdd) {
	                	// finally, store it
	                    classSet.add(c.asSubclass(clazz));
                	}
                }
            } catch (ClassNotFoundException e) {  //ignore, move on
                continue;
            }
        }
        return classSet;
    }

    private static Collection<String> searchRegularDirectory (String packagePath, final Pattern p) {
        // regular file structure, recursively seek classes to add to list
        List<String> foundList = new ArrayList<String>();
        Queue<File> searchQue = new LinkedList<File>();
        /* because we're search-replacing a File path using packagePath, need
         * to do special handling for DOS; we might start with packagePath
         * "/X:/path/to/package" or "//srv/path/to/package", which need to
         * sanitize to become "[X:]\path\to\package..." */
        if (! File.separator.equals("/")) {
        	// remove leading '/' in "/X:" and then replace '/' with '\\'
        	packagePath = packagePath.replaceFirst("^/(\\w+:)", "$1").replace("/", File.separator);
        }  // now we should have packagePath "[X:]\path\to\package"
        /* make sure package path ends with a separator so the matched filenames
         * properly get converated to class names. */
        if (! packagePath.endsWith(File.separator)) {
        	packagePath = packagePath + File.separator;
        }

        searchQue.add(new File(packagePath));
        while (!searchQue.isEmpty()) {
            File searchDir = searchQue.poll();
            Set<File> containedFiles = new HashSet<File>();  // all children
            Collections.addAll(containedFiles, searchDir.listFiles());
            Set<File> matchedFiles = new HashSet<File>();  // matching children
            Collections.addAll(matchedFiles, searchDir.listFiles(new FilenameFilter() {
                public boolean accept (File dir, String name) {
                    // find all matching classes
                    return p.matcher(name).matches();
                }
            }));
            for (File f : matchedFiles) {
                // remove relative path to package, then replace seps with '.'
                foundList.add(f.getPath().replace(packagePath, "").replace(File.separator, "."));
            }
            // find all subdirectories to search under
            containedFiles.removeAll(matchedFiles);  // avoid duplicate search
            for (File f : containedFiles) {
                if (f.isDirectory()) {
                    searchQue.offer(f);
                }
            }
        }
        return foundList;
    }

    /**
     * Searches and returns within the classpaths the list of resource paths
     * containing the supplied Class, inclusive of package directories.
     * <p>
     * In the current implementation, if application is executed within a
     * directory context (i.e., development environment), it'll simply return
     * the directory containing &lt;package&gt;<code>clazz</code>.
     * Otherwise, when executed in a Jar context (i.e., deployment), it'll
     * return the Jar file containing <code>clazz</code>, as matched by the pattern
     * <blockquote>
     * <code>^(.*)&lt;packageAsDir&gt;/&lt;rname$&gt;</code>
     * </blockquote>where '/' is actually {@link File#separator}.
     * </p>
     * @param clazz  the class whose resource to find
     * @param rname  name of resource sought
     * @return  String array of resource paths
     */
    public static String[] resourcePaths (Class<?> clazz, String rname) {
        List<String> pathList = new ArrayList<String>();
        String packageName = clazz.getPackage().getName();
        // construct a regex pattern that captures everything before package
        String fullPattern = "^(.*)" + packageName.replace(".", "/")
                + "/" + rname + ".*";
        final Pattern p = Pattern.compile(fullPattern);

        // collect the set of paths to search from classpath
        List<String> searchList = new ArrayList<String>();
        searchList.add(".");  // add current path in case it's a regular dir
        String userdir = System.getProperty("user.dir");
        StringTokenizer tokens = new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator);
        while (tokens.hasMoreTokens()) {
            String path = tokens.nextToken();
            // replace all DOS file-seps with Unix file-sep 
            searchList.add(path.replaceAll("\\\\", "/"));
        }

        // now search all the candidate paths
        for (String path : searchList) {
            if (path.equals(".")) {
                URL packageURL = clazz.getResource(rname);
                if (packageURL != null) {
                    Matcher m = p.matcher(packageURL.getPath());
                    if (m.matches()) {
                        pathList.add(m.group(1));
                    }
                }
            } else {  // try looking inside Jar!
                String abspath = null;  // make path absolute first
                if (new File(path).isAbsolute()) {  // already an absolute path
                    abspath = path;
                } else {  // make relative to user path
                    try {
                        abspath = new File(userdir + File.separator + path).getCanonicalPath();
                    } catch (IOException e) {
                        abspath = path;  // can't do much more
                    }
                }
                if (path.endsWith(".jar")) {  // look inside this jar for this class
                    String jarpath = String.format("jar:file:%s!/", abspath);
                    try {
                        JarURLConnection conn = (JarURLConnection)new URL(jarpath).openConnection();
                        JarFile jarfile = conn.getJarFile();
                        for (JarEntry je : Collections.list(jarfile.entries())) {
                            if (p.matcher(je.getName()).matches()) {
                                pathList.add(jarpath);  // done
                                break;
                            }
                        }
                    } catch (MalformedURLException e) {  // ignore error
                    } catch (IOException e) {  // ignore error
                    }
                } else {
                    Matcher m = p.matcher(abspath);
                    if (m.matches()) {
                        pathList.add(m.group(1));
                    }
                }
            }
        }

        pathList.add(userdir);  // finally, look inside working dir for overrides
        return pathList.toArray(new String[0]);
    }

    /**
     * Given a file object, ensure that all directories exist within the path
     * up to, but excluding, the final file segment.  This function has the side
     * effect that any non-existent directories in the path will be created.
     * 
     * @param f  file whose entire path to check.
     */
    public static File ensureDirectoriesExist (File f) throws IOException {
        File canonFile = f.getCanonicalFile();
        canonFile.getParentFile().mkdirs();
        return canonFile;
    }

    /**
     * Returns system-property boolean with the designated default value if
     * not defined.  Convenience method for:<blockquote>
     * {@code Boolean.valueOf(System.getProperty(propName, Boolean.toString(def)))}
     * </blockquote>
     * @param propName  system property get to
     * @param def       the default boolean to return
     * @return <code>true</code> if system property is set to true, <code>false</code> otherwise.
     */
    public static boolean getSysBoolWithDefault (String propName, boolean def) {
        return Boolean.valueOf(System.getProperty(propName, Boolean.toString(def)));
    }

    /**
     * Utility method to look for any key=value attribute for a Node and returns
     * null if the attribute does not exist or contains the empty string.
     * 
     * @param thisNode  the Node for which to find desired attribute
     * @return String corresponding to the <code>key</code>= attribute,
     *         trimmed, or null if there is not a valid name.
     */
    public static String getNodeAttribute (Node thisNode, String key) {
        if (null == thisNode) {
            return null;
        }
        NamedNodeMap attributes = thisNode.getAttributes();
        if (null == attributes) {
            return null;
        }
        Node node = attributes.getNamedItem(key);
        if (null == node) {
            return null;
        }
        String value = node.getNodeValue();
        if (null == value
                || (null != value && value.trim().length() == 0)) {
            return null;
        }

        assert value != null;  // value NOT null at this point
        return value.trim();
    }

    /**
     * Compares two version strings, converting to double if possible.
     * @param ver1  first version string
     * @param ver2  second version string
     * @return  <code>0</code> if equal, <code>-1</code> if ver1 < ver2, or <code>1</code> if ver1 > ver2.
     */
    public static int compareVersions (String ver1, String ver2) {
        return Version.parseVersion(ver1).compareTo(Version.parseVersion(ver2));
    }

}
