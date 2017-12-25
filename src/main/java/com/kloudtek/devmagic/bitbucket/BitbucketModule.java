package com.kloudtek.devmagic.bitbucket;

import com.google.auto.service.AutoService;
import com.kloudtek.devmagic.ComplexCommand;
import com.kloudtek.devmagic.Module;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Collections;
import java.util.List;

@Command(name = "bb", description = "BitBucket tools")
@AutoService(Module.class)
public class BitbucketModule extends ComplexCommand implements Module, Runnable {
    @Option(names = {"-u", "--user"}, description = "Bitbucket username")
    private String username;

    @Override
    public String getCmdName() {
        return "bb";
    }

    @Override
    public List<Object> getSubcommands() {
        return Collections.singletonList(new BitBucketExec(this));
    }
}
