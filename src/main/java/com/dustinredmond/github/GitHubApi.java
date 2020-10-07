package com.dustinredmond.github;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.GitHubService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.List;

public class GitHubApi {

    private static GitHubApi instance;
    private static final GitHubClient client = new GitHubClient();
    private static final GistService gistService = new GistService(client);
    private static final UserService userService = new UserService(client);
    private static final RepositoryService repositoryService = new RepositoryService(client);

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
        return gistService;
    }

    public RepositoryService getRepositoryService() { return repositoryService; }

    public List<Gist> getGists() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        try {
            return new GistService(client).getGists(currentUser.getLogin());
        } catch (IOException e) {
            return null;
        }
    }

    public static User getCurrentUser() {
        try {
            return new UserService(client).getUser();
        } catch (IOException e) {
            return null;
        }
    }


}
