package ee.ria.xroad.proxyui;

import java.io.File;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ee.ria.xroad.common.SystemProperties;
import ee.ria.xroad.common.conf.globalconf.GlobalConf;
import ee.ria.xroad.common.util.FileContentChangeChecker;

/**
 * Job that checks whether identifier mapping has changed.
 */
@DisallowConcurrentExecution
public class IdentifierMappingChecker implements Job {

    private static final File XROAD_INSTALLED_FILE = new File(
            "/usr/xtee/etc/v6_xroad_installed");

    private static final File XROAD_PROMOTED_FILE = new File(
            "/usr/xtee/etc/v6_xroad_promoted");

    private static final Logger LOG =
            LoggerFactory.getLogger(IdentifierMappingChecker.class);

    private static FileContentChangeChecker identifierMappingChecker;

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        try {
            if (XROAD_INSTALLED_FILE.isFile()) {
                checkIdentifierMapping();
            }
        } catch (Exception e) {
            LOG.error("Checking identifier mapping for updates failed", e);
            throw new JobExecutionException(e);
        }
    }

    private void checkIdentifierMapping() throws Exception {
        if (identifierMappingChecker != null
                && !identifierMappingChecker.hasChanged()) {
            LOG.debug("Identifier mapping has not been changed");
            return;
        }

        LOG.debug("Identifier mapping changed");

        identifierMappingChecker =
                new FileContentChangeChecker(getIdentifierMappingFile());

        String command = XROAD_PROMOTED_FILE.isFile()
                ? SystemProperties.getServiceExporterCommand()
                : SystemProperties.getServiceImporterCommand();

        if (command == null) {
            return;
        }

        LOG.debug("Running command '{}'", command);

        if (Runtime.getRuntime().exec(command).waitFor() != 0) {
            LOG.error("Command returned error code, see serviceimporter log");
        }
    }

    private static String getIdentifierMappingFile() {
        return GlobalConf.getInstanceFile("identifiermapping.xml").toString();
    }
}