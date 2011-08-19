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

package uk.ac.ncl.cs.harness;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import uk.ac.ncl.cs.harness.extsupport.ExtensionRegistry;
import uk.ac.ncl.cs.harness.io.OutputController;
import uk.ac.ncl.cs.harness.io.ProtocolParser;
import uk.ac.ncl.cs.harness.workflow.Protocol;
import uk.ac.ncl.cs.harness.workflow.ConcurrentWorkflow;

/**
 * The integration test harness main class.
 *
 * @author Jochen Weile
 */
public class Main {

    /**
     * Main method.
     * @param args expects the name of the protocol.xml file to be used.
     */
    public static void main(String[] args) {

        LoggingInit.init();

        try {

            Main main = new Main();

            main.run(main.processArgs(args));

        } catch (Throwable t) {
            logProcessedError(t);
            Logger.getLogger(Main.class.getName()).log(Level.FINE, t.getMessage(), t);
            System.exit(1);

        }
        
    }

    private static void logProcessedError(Throwable t) {
        StringBuilder b = new StringBuilder(256);

        b.append(t.getMessage());
        Throwable cause = t;
        while ((cause = cause.getCause()) != null) {
            b.append("\nReason: ").append(cause.getMessage());
        }

        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, b.toString());
    }

    private void run(CommandLine cmd) throws Exception {


        //init extension registry
        ExtensionRegistry.getInstance();

        //get protocol file pointer
        File protocolFile = new File((String)cmd.getArgList().get(cmd.getArgList().size() -1));
        String tag = cmd.getOptionValue("t", "");
        int cpus = 1;
        try {
            cpus = Integer.parseInt(cmd.getOptionValue("c", "1"));
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("CPU argument must be an integer.",nfe);
        }

        //validate and parse protocol
        ProtocolParser parser = new ProtocolParser();
        parser.validate(protocolFile);
        Protocol protocol = parser.parse(protocolFile);

        System.out.println(HEADER);

        //init output directory
        OutputController.getInstance().prepareOutput(tag, protocolFile);

        //execute protocol
        ConcurrentWorkflow workflow = new ConcurrentWorkflow(cpus);
        System.out.println("Executing protocol \""+tag+"\"...");
        workflow.run(protocol);
        
        //close output streams
//        OutputController.getInstance().closeResultStream("rateDeviations.tsv");
//        OutputController.getInstance().closeResultStream("loss.tsv");
    }

    private CommandLine processArgs(String[] args) {

        Options options = new Options();

        options.addOption("h", "help", false,
                            "Print this usage information");
        options.addOption("t", "tag", true, 
                            "A tag for this session.");
        options.addOption("c", "cpus", true, "Specify the number of CPUs to use.");


        CommandLineParser cp = new PosixParser();
        try {

            CommandLine cmd = cp.parse(options, args);

            if (cmd.hasOption('h')) {
                printUsage(options);
                System.exit(0);
            }

            return cmd;
        } catch (ParseException e) {

            printUsage(options);
            System.exit(1);

        }

        //FIXME: Unreachable. Bad style
        return null;

    }

    private static final String USAGE = "[-options] <protocol-file>";
    private static final String HEADER =
                          "harness - A probabilistic integration test suite, Copyright 2011 Jochen Weile.";
    private static final String FOOTER =
                          "Report bugs to j.weile@ncl.ac.uk";

    private void printUsage(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setWidth(80);
        helpFormatter.printHelp(USAGE, HEADER, options, FOOTER);
    }


}
