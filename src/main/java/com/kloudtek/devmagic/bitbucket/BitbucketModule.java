package com.kloudtek.devmagic.bitbucket;

import com.google.auto.service.AutoService;
import com.kloudtek.devmagic.ComplexCmd;
import com.kloudtek.devmagic.DevMagicCli;
import com.kloudtek.devmagic.Module;
import com.kloudtek.devmagic.util.FilterUtils;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Command(name = "bb", description = "BitBucket tools")
@AutoService(Module.class)
public class BitbucketModule extends ComplexCmd implements Module {
    private static final Logger logger = LoggerFactory.getLogger(BitbucketModule.class);
    @Option(names = {"-u", "--user"}, description = "© Bitbucket username")
    private String username;
    @Option(names = {"-p", "--password"}, description = "© Bitbucket password")
    private String password;
    @Override
    public String getCmdName() {
        return "bb";
    }

    @Override
    public List<Object> getSubcommands() {
        return Arrays.asList(new BitBucketExec(this), new BitBucketList(this));
    }

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
            Map<String, Object> resp = DevMagicCli.readJsonObject(response.returnContent().asString());
            url = (String) resp.get("next");
            for (Map<String, Object> proj : (List<Map<String, Object>>) resp.get("values")) {
                names.add((String) proj.get("name"));
            }
        }
        return FilterUtils.filter(names, includes, excludes);
    }
}
