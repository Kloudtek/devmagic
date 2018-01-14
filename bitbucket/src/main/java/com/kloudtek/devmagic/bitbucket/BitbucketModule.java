package com.kloudtek.devmagic.bitbucket;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.kloudtek.devmagic.DevMagicPluginCommand;
import com.kloudtek.ktcli.CliHelper;
import com.kloudtek.util.FilterUtils;
import com.kloudtek.util.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Command(name = "bb", description = "BitBucket tools", subcommands = {BitBucketList.class, BitBucketExec.class})
@AutoService(DevMagicPluginCommand.class)
public class BitbucketModule extends DevMagicPluginCommand {
    private static final Logger logger = LoggerFactory.getLogger(BitbucketModule.class);
    @Option(names = {"-u", "--user"}, description = "© Bitbucket username")
    @JsonProperty
    private String username;
    @Option(names = {"-p", "--password"}, description = "© Bitbucket password")
    @JsonProperty
    private String password;

    public Executor getHttpExecutor() {
        HttpHost host = new HttpHost("api.bitbucket.org", 443, "https");
        return Executor.newInstance().auth(host, username, password).authPreemptive(host);
    }

    @SuppressWarnings("unchecked")
    public List<String> listProjectsNames(String team, List<String> includes, List<String> excludes) throws IOException {
        ArrayList<String> names = new ArrayList<>();
        String url = "https://api.bitbucket.org/2.0/repositories";
        if (StringUtils.isNotEmpty(team)) {
            url = url + "/" + team;
        }
        while (url != null) {
            logger.debug("Retrieving project pages from bitbucket: " + url);
            Response response = getHttpExecutor().execute(Request.Get(url)
                    .addHeader("Content-Type", "application/json"));
            Map<String, Object> resp = CliHelper.readJsonObject(response.returnContent().asString());
            url = (String) resp.get("next");
            for (Map<String, Object> proj : (List<Map<String, Object>>) resp.get("values")) {
                names.add((String) proj.get("name"));
            }
        }
        return FilterUtils.regexFilter(names, includes, excludes);
    }
}
