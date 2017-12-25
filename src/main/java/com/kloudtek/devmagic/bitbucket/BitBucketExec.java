package com.kloudtek.devmagic.bitbucket;

import com.kloudtek.util.UserDisplayableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;

@Command(name = "exec", description = "Checkout multiple repositories, execute command and commit/push modifications")
public class BitBucketExec extends BitBucketList {
    private static final Logger logger = LoggerFactory.getLogger(BitBucketExec.class);
    @Parameters(description = "File to execute")
    private File exec;

    public BitBucketExec(BitbucketModule bitbucketModule) {
        super(bitbucketModule);
    }

    @Override
    public Object call() throws Exception {
        if (exec.exists()) {
            throw new UserDisplayableException("Exec file not found: " + exec);
        }
        for (String name : bitbucketModule.listProjectsNames(team, includes, excludes)) {
//            ProcessExecutionResult res = ProcessUtils.exec(exec, name);
//            logger.info(res.getStdout().trim());
        }
        return null;
    }
}
