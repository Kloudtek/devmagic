package com.kloudtek.devmagic.bitbucket;

import picocli.CommandLine;
import com.kloudtek.util.UserDisplayableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

@CommandLine.Command(name = "exec", description = "Checkout multiple repositories, initAndRun command and commit/push modifications")
public class BitBucketExec extends BitBucketList {
    private static final Logger logger = LoggerFactory.getLogger(BitBucketExec.class);
    @CommandLine.Parameters(description = "File to initAndRun")
    private File exec;

    @Override
    public void execute() {
        System.out.println("XXXX ");
        if (exec.exists()) {
            throw new UserDisplayableException("Exec file not found: " + exec);
        }
//        for (String name : bitbucketModule.listProjectsNames(team, includes, excludes)) {
//            ProcessExecutionResult res = ProcessUtils.exec(exec, name);
//            logger.info(res.getStdout().trim());
//        }
    }
}
