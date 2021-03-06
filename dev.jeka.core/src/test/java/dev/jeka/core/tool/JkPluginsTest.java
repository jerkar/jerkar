package dev.jeka.core.tool;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class JkPluginsTest {

    @Test
    public void testPluginsLoading() {
        final PluginDictionary plugins = new PluginDictionary();
        final Set<PluginDictionary.PluginDescription> pluginSet = plugins.getAll();

        Assert.assertTrue(pluginSet.toString(), pluginSet.size() > 6);
    }

    interface PluginBase {
    }

    @SuppressWarnings("unused")
    private static class PluginBaseMy implements PluginBase {

    }

}
