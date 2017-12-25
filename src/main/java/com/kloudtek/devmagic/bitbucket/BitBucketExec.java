package com.kloudtek.devmagic.bitbucket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(name = "exec", description = "Checkout multiple repositories, execute command and commit/push modifications")
public class BitBucketExec implements Callable<Object> {
    private static final Logger logger = LoggerFactory.getLogger(BitBucketExec.class);
    @Parameters(description = "Command to execute (directory of clone repository will be provided as first argument)")
    private String command;
    private BitbucketModule bitbucketModule;

    public BitBucketExec(BitbucketModule bitbucketModule) {
        this.bitbucketModule = bitbucketModule;
    }

    @Override
    public Object call() throws Exception {
        return null;
    }
}
