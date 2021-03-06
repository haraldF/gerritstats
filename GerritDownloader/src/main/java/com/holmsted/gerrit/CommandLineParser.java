package com.holmsted.gerrit;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class CommandLineParser {

    private static final String DEFAULT_OUTPUT_DIR = "out";

    public static class ServerAndPort {
        private String serverName;
        private int serverPort;

        public static class Converter implements IStringConverter<ServerAndPort> {
            @Override
            public ServerAndPort convert(String value) {
                ServerAndPort result = new ServerAndPort();
                result.serverName = value;
                int portSeparator = result.serverName.indexOf(':');
                if (portSeparator != -1) {
                    result.serverPort = Integer.valueOf(result.serverName.substring(portSeparator + 1));
                    result.serverName = result.serverName.substring(0, portSeparator);
                }
                return result;
            }
        }
    }

    @Parameter(names = "--server",
            description = "Read output from Gerrit server URL and given port, in format server:port. "
                    + "If port is omitted, defaults to 29418.",
            arity = 1,
            required = true,
            converter = ServerAndPort.Converter.class)
    private ServerAndPort serverAndPort;

    @Parameter(names = "--project",
            description = "The Gerrit project from which to retrieve stats. "
                    + "If omitted, stats will be retrieved from all projects.")
    private String projectName;

    @Parameter(names = "--output-file",
            description = "The file into which the json output will be written into.",
            required = true)
    private String outputFile;

    @Parameter(names = "--limit",
            description = "The number of commits which to retrieve from the server. "
            + "If omitted, stats will be retrieved until no further records are available.")
    private int limit = GerritStatReader.NO_COMMIT_LIMIT;

    @Nonnull
    private final JCommander jCommander = new JCommander(this);

    public boolean parse(String[] args) {
        try {
            jCommander.parse(args);
        } catch (ParameterException e) {
            return false;
        }

        return getServerName() != null;
    }

    @Nonnull
    private static String resolveOutputDir(@Nonnull String path) {
        if (path.startsWith("~" + File.separator)) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    @Nullable
    public String getOutputFile() {
        return outputFile;
    }

    @Nullable
    public String getServerName() {
        return serverAndPort != null ? serverAndPort.serverName : null;
    }

    @Nullable
    public String getProjectName() {
        return projectName;
    }

    public int getServerPort() {
        return serverAndPort != null ? serverAndPort.serverPort : 0;
    }

    public int getCommitLimit() {
        return limit;
    }

    public void printUsage() {
        jCommander.usage();
    }
}
