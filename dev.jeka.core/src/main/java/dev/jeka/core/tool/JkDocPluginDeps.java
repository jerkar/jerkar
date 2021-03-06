package dev.jeka.core.tool;

import java.lang.annotation.*;

/**
 * Mentions that the annotated plugin class relies on other plugins.
 *
 * @author Jerome Angibaud
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface JkDocPluginDeps {

    /**
     * Plugins classes this plugin relies on.
     */
    Class<? extends JkPlugin>[] value();

}
