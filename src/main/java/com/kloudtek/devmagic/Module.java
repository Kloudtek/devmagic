package com.kloudtek.devmagic;

import picocli.CommandLine;

public interface Module {
    void init(CommandLine cmd);
}
