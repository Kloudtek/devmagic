package com.kloudtek.devmagic.bitbucket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "ls", description = "list available repositories")
public class BitBucketList implements Callable<Object> {
    private static final Logger logger = LoggerFactory.getLogger(BitBucketList.class);
    @Option(names = {"-t", "--team"}, description = "BitBucket team")
    protected String team;
    @Option(names = {"-i", "--includes"}, description = "Only include project names that match this regex value")
    protected List<String> includes;
    @Option(names = {"-e", "--excludes"}, description = "Exclude project names that match this regex value")
    protected List<String> excludes;
    protected BitbucketModule bitbucketModule;

    public BitBucketList(BitbucketModule bitbucketModule) {
        this.bitbucketModule = bitbucketModule;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object call() throws Exception {
        for (String name : bitbucketModule.listProjectsNames(team, includes, excludes)) {
            System.out.println(name);
        }
        return null;
    }
}
