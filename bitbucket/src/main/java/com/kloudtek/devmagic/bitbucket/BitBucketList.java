package com.kloudtek.devmagic.bitbucket;

import com.kloudtek.ktcli.CliCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Command(name = "ls", description = "list available repositories")
public class BitBucketList extends CliCommand<BitbucketModule> {
    private static final Logger logger = LoggerFactory.getLogger(BitBucketList.class);
    @Option(names = {"-t", "--team"}, description = "BitBucket team")
    protected String team;
    @Option(names = {"-i", "--includes"}, description = "Only include project names that match this regex value")
    protected List<String> includes;
    @Option(names = {"-e", "--excludes"}, description = "Exclude project names that match this regex value")
    protected List<String> excludes;

    @Override
    public void execute() throws Exception {
        for (String name : parent.listProjectsNames(team, includes, excludes)) {
            System.out.println(name);
        }
    }
}
