/*
 *  Copyright (C) 2011 Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ncl.cs.harness.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The OutputController is a central singleton that controls harness's output 
 * behaviour. The singleton is obtained by calling the 
 * <code>OutputController.getInstance();</code> method. 
 * The <code>prepareOutput(String tag, File protocolFile)</code> can then be used
 * to initialize a central output directory, which will contain the protocol input file,
 * the workflow log file and all output files generated using the <code>writeResults</code>
 * and <code>writeResultStream</code> methods.
 *
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class OutputController {

    /**
     * current output directory.
     */
    private File currOutputDir;

    /**
     * singleton.
     */
    private static OutputController instance;

    /**
     * private constructor to enforce singleton.
     */
    private OutputController() {

    }

    /**
     * singleton.
     */
    public static OutputController getInstance() {
        if (instance == null) {
            instance = new OutputController();
        }
        return instance;
    }


    /**
     * prepares the output directory for this session.
     * @param protocolFile
     * @throws IOException
     */
    public void prepareOutput(String tag, File protocolFile) throws IOException {
        
        //create current output folder
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String dirName = df.format(new Date())+"_"+tag;
        currOutputDir = new File(ResourcePaths.OUTPUT_DIR, dirName);
        currOutputDir.mkdirs();

        //set up second log file
        File logFile = new File(currOutputDir, "execution.log");
        FileHandler fh = new FileHandler(logFile.getAbsolutePath());
        fh.setLevel(Level.CONFIG);
        Logger.getLogger("").addHandler(fh);

        //copy protocol
        if (protocolFile != null) {//can be null for JUnit tests.
            File protocolCopy = new File(currOutputDir, "protocol.xml");
            copy(protocolFile, protocolCopy);
        }

    }


//    /**
//     * returns the current output directory.
//     */
//    public File getCurrentOutputDirectory()  {
//        return currOutputDir;
//    }

    /**
     * Copies the contents of the source file to the target file.
     * @param source the source file.
     * @param target the target file.
     * @throws IOException if an I/O error occurrs.
     */
    private void copy(File source, File target) throws IOException {

        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(target);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
        
    }

    /**
     * Write a string to a file and close it afterwards.
     * @param fileName
     * @param resultString
     * @throws IOException
     */
    public void writeResults(String fileName, String resultString) throws IOException {
        BufferedWriter w = null;
        try {
            File outFile = new File(currOutputDir, fileName);
            w = new BufferedWriter(new FileWriter(outFile));
            w.write(resultString);
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    /**
     * An internal registry for file output streams that are currently open.
     */
    private Map<String,BufferedWriter> writers = new HashMap<String,BufferedWriter>(8);

    /**
     * writes strings to output files and leaves the file handles open to be used again later on.
     * @param fileName the file name
     * @param contents a string that will be written to the file
     * @throws IOException if an I/O error occurs
     */
    public synchronized void writeResultStream(String fileName, String contents) throws IOException {
        BufferedWriter w = writers.get(fileName);
        if (w == null) {
            File outFile = new File(currOutputDir, fileName);
            if (outFile.exists()) {
                throw new IOException("Output file destination already exists: "+outFile.getCanonicalPath());
            }
            w = new BufferedWriter(new FileWriter(outFile));
            writers.put(fileName, w);
        }
        w.write(contents);
    }

    /*
     * registers a shutdown hook which will close all remaining file handles
     * during JVM shutdown procedure.
     */
    static {
        Thread shutdownHook = new Thread() {
            OutputController out = OutputController.getInstance();
            @Override
            public void run() {
                try {
                    out.closeResultStreams();
                } catch (Throwable t) {
                    Logger.getLogger(OutputController.class.getName()).log(Level.SEVERE, null, t);
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /**
     * closes all streams. only to be used by the shutdown hook.
     */
    private void closeResultStreams()  {
        for (BufferedWriter w : writers.values()) {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException ex) {
                    Logger.getLogger(OutputController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * closes and de-registers a file handle.
     * @param id
     * @throws IOException
     */
    public void closeResultStream(String id) throws IOException {
        BufferedWriter w = writers.get(id);
        if (w != null) {
            w.close();
            writers.remove(w);
        }
    }




}
