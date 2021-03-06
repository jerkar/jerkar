package dev.jeka.core.api.tooling;

import dev.jeka.core.api.depmanagement.JkVersion;
import dev.jeka.core.api.system.JkLog;
import dev.jeka.core.api.system.JkProcess;
import dev.jeka.core.api.utils.JkUtilsAssert;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Wrapper for Git command line interface. This class assumes Git is installed on the host machine.
 */
public final class JkGitWrapper {

    private final JkProcess git;

    private JkGitWrapper(JkProcess process) {
        this.git = process.withFailOnError(false);
    }

    public static JkGitWrapper of(Path dir) {
        return new JkGitWrapper(JkProcess.of("git").withWorkingDir(dir).withFailOnError(true));
    }

    public static JkGitWrapper of(String dir) {
        return of(Paths.get(""));
    }

    public static JkGitWrapper of() {
        return of("");
    }

    public JkGitWrapper withLogCommand(boolean log) {
        return new JkGitWrapper(this.git.withLogCommand(log));
    }

    public JkGitWrapper withFailOnError(boolean fail) {
        return new JkGitWrapper(this.git.withFailOnError(fail));
    }

    public JkGitWrapper withLogOutput(boolean log) {
        return new JkGitWrapper(this.git.withLogOutput(log));
    }

    public String getCurrentBranch() {
        return git.andParams("rev-parse", "--abbrev-ref", "HEAD").withLogOutput(false).runAndReturnOutputAsLines().get(0);
    }

    public boolean isRemoteEqual() {
        Object local = git.andParams("rev-parse", "@").runAndReturnOutputAsLines();
        Object remote = git.andParams("rev-parse", "@{u}").runAndReturnOutputAsLines();
        return local.equals(remote);
    }

    public boolean isWorkspaceDirty() {
        return !git.andParams("diff", "HEAD", "--stat").withLogOutput(false).runAndReturnOutputAsLines().isEmpty();
    }

    public String getCurrentCommit() {
        return git.andParams("rev-parse", "HEAD").withLogOutput(false).runAndReturnOutputAsLines().get(0);
    }

    public List<String> getTagsOfCurrentCommit() {
        return git.andParams("tag", "-l", "--points-at", "HEAD").withLogOutput(false).runAndReturnOutputAsLines();
    }

    public List<String> getLastCommitMessage() {
        return git.andParams("log", "--oneline", "--format=%B", "-n 1", "HEAD").withLogOutput(false).runAndReturnOutputAsLines();
    }

    /**
     * Convenient method to extract information from the last commit message title.
     * This splits title is separated words, then looks for the fist word starting
     * with the specified prefix. The returned suffix is the word found minus the prefix.<p/>
     * This method returns <code>null</code> if no such prefix found.
     *
     * For example, if the title is 'Release:0.9.5.RC1 : Rework Dependencies', then
     * invoking this method with 'Release:' argument will return '0.9.5.RC1'.
     */
    public String extractSuffixFromLastCommitMessage(String prefix) {
        List<String> messageLines = getLastCommitMessage();
        if (messageLines.isEmpty()) {
            return null;
        }
        String[] words = messageLines.get(0).split(" ");
        for (String word : words) {
            if (word.startsWith(prefix)) {
                return word.substring(prefix.length());
            }
        }
        return null;
    }

    public JkGitWrapper tagAndPush(String name) {
        tag(name);
        git.andParams("push", "origin", name).runSync();
        return this;
    }

    public JkGitWrapper tag(String name) {
        git.andParams("tag", name).runSync();
        return this;
    }

    /**
     * Returns version according the last commit message. If the commit message contains a word
     * starting with the specified prefix keyword then the substring following this suffix will be
     * returned.<br/>
     * If no such prefix found, then a version formatted as [branch]-SNAPSHOT will be returned
     */
    public String getVersionFromCommitMessage(String prefixKeyword) {
        String afterSuffix = extractSuffixFromLastCommitMessage(prefixKeyword);
        if (afterSuffix != null) {
            return afterSuffix;
        }
        String branch;
        try {
            branch = getCurrentBranch();
            return branch + "-SNAPSHOT";
        } catch (IllegalStateException e) {
            JkLog.warn(e.getMessage());
            return JkVersion.UNSPECIFIED.getValue();
        }
    }


    /**
     * If the current commit is tagged then the version is the tag name (last in alphabetical order). Otherwise
     * the version is [branch]-SNAPSHOT
     */
    public String getVersionFromTag() {
        List<String> tags;
        String branch;
        try {
            tags = getTagsOfCurrentCommit();
            branch = getCurrentBranch();
        } catch (IllegalStateException e) {
            JkLog.warn(e.getMessage());
            return JkVersion.UNSPECIFIED.getValue();
        }
        if (tags.isEmpty()) {
            return branch + "-SNAPSHOT";
        } else {
            return tags.get(tags.size() - 1);
        }
    }

    /**
     * @see #getVersionFromTag()
     */
    public JkVersion getJkVersionFromTag() {
        return JkVersion.of(getVersionFromTag());
    }

    public JkGitWrapper exec(String... args) {
        JkProcess gitCommand = git.andParams(args);
        int code = gitCommand.runSync();
        JkUtilsAssert.state(code == 0 || !git.isFailOnError(), "Command " + gitCommand + " returned with error " + code);
        return this;
    }

}
