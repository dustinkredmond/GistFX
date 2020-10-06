package com.dustinredmond.github;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;

import java.io.IOException;
import java.util.List;

public class GitHubApi {

    private static GitHubApi instance;
    private static GitHubClient client = new GitHubClient();

    private GitHubApi() { super(); }

    public static GitHubApi getInstance() {
        if (instance == null) {
            instance = new GitHubApi();
        }
        return instance;
    }

    public GitHubClient getGitHubClient() {
        return client;
    }

    public GistService getGistService() {
        return new GistService(client);
    }

    public List<Gist> getGists() {
        try {
            return new GistService(client).getGists(client.getUser());
        } catch (IOException e) {
            return null;
        }
    }


}
