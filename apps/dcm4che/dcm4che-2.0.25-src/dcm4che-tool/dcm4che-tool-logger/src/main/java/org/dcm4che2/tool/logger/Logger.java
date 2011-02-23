/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che2.tool.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 5551 $ $Date: 2007-11-27 15:24:27 +0100 (Tue, 27 Nov 2007) $
 * @since May 26, 2006
 * 
 */
public class Logger {

    
    private static final String[] USAGE = {
        "Usage: logger [-p ms] {fewidV} files ...",
        "Log files to destination specified in etc/logger/log4j.properties",
        "Options:",
        "    -p  pause specified time between emission of following messages",
        "    -f  log files with FATAL level",
        "    -e  log files with ERROR level",
        "    -w  log files with WARN level",
        "    -i  log files with INFO level",
        "    -d  log files with DEBUG level",
        "    -V  print the version information and exit" };
    
    private static long sleep = 0;


    private static void usage() {
        for (int i = 0; i < USAGE.length; i++) {
            System.out.println(USAGE[i]);
        }        
    }
    
    public static void main(String[] args) {
        char opt = 0;
        int argi = 0;
        long pause = 0;
        try {
            String s;
            char c;
            int pos;
            for (;;) {
                s = args[argi];
		c = s.charAt(pos=0);
                if (c == '-') {
                    c = s.charAt(pos=1);
                }
                if (c == 'p') {
                    pause = Long.parseLong(s.length() > pos+1 
                	    ? s.substring(pos+1) : args[++argi]);
                } else {
                    if (opt != 0) {
                	break;
                    }
                    opt = c;
                }
                ++argi;
            }
        } catch (IndexOutOfBoundsException e) {
            usage();
            return;
        }
        if (opt == 'V') {
            Package p = Logger.class.getPackage();
            System.out.println("logger v" + p.getImplementationVersion());
            return;
        }
        if ("fewid".indexOf(opt) == -1 || argi >= args.length) {
            usage();
            return;
        }
        org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Logger.class);
        long start = System.currentTimeMillis();
        int n = 0;
        for (int i = argi; i < args.length; i++) {
            n += logFile(log, opt, pause, new File(args[i]));
        }
        long end = System.currentTimeMillis();
        System.out.println("\nEmit " + n + " messages in " + (end-start) + "ms.");
    }

    private static int logFile(org.apache.log4j.Logger log, char opt, long pause, File f) {
        if (f.isDirectory()) {
            int n = 0;
            String[] ss = f.list();
            for (int i = 0; i < ss.length; i++) {
                n += logFile(log, opt, pause, new File(f, ss[i]));
            }
            return n;
        }
        String msg;
        try {
            msg = readFile(f);
        } catch (Exception e) {
            System.err.println("Failed to read " + f);
            e.printStackTrace(System.err);
            return 0;
        }
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException ignore) {
            // explicitly interrupted by another thread; continue
        }
        switch (opt) {
        case 'd':
            log.debug(msg);
            break;
        case 'e':
            log.error(msg);
            break;
        case 'f':
            log.fatal(msg);
            break;
        case 'i':
            log.info(msg);
            break;
        }
        System.out.print('.');
        sleep = pause;
        return 1;
    }

    private static String readFile(File f) throws IOException {
        byte[] b = new byte[(int) f.length()];
        FileInputStream is = new FileInputStream(f);
        try {
            for (int off = 0; off < b.length;) {
                off += is.read(b, off, b.length - off);
            }
        } finally {
            is.close();
        }
        return new String(b);
    }

}
