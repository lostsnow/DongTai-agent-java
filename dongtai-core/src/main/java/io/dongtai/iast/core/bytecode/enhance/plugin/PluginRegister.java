package io.dongtai.iast.core.bytecode.enhance.plugin;

import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.authentication.shiro.DispatchShiro;
import io.dongtai.iast.core.bytecode.enhance.plugin.core.DispatchClassPlugin;
import io.dongtai.iast.core.bytecode.enhance.plugin.framework.dubbo.DispatchDubbo;
import io.dongtai.iast.core.bytecode.enhance.plugin.framework.feign.DispatchFeign;
import io.dongtai.iast.core.bytecode.enhance.plugin.framework.j2ee.dispatch.DispatchJ2ee;
import io.dongtai.iast.core.bytecode.enhance.plugin.hardcoded.DispatchHardcodedPlugin;
import io.dongtai.iast.core.bytecode.enhance.plugin.service.jdbc.DispatchJdbc;
import io.dongtai.iast.core.bytecode.enhance.plugin.service.kafka.DispatchKafka;
import io.dongtai.iast.core.bytecode.enhance.plugin.spring.DispatchApiCollector;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import io.dongtai.iast.core.handler.hookpoint.models.policy.PolicyManager;
import org.objectweb.asm.ClassVisitor;

import java.util.*;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class PluginRegister {

    /**
     * 定义PLUGINS常量，用于存储定义的字节码修改类
     */
    private final List<DispatchPlugin> plugins;

    public PluginRegister() {
        this.plugins = new ArrayList<>();
        List<String> disabledPlugins = getdisabledPlugins();
        List<DispatchPlugin> allPlugins = new ArrayList<>(Arrays.asList(
                new DispatchApiCollector(),
                new DispatchJ2ee(),
                new DispatchKafka(),
                new DispatchJdbc(),
                new DispatchShiro(),
                new DispatchFeign(),
                new DispatchDubbo()
        ));
        allPlugins.removeIf(plugin -> disabledPlugins != null && disabledPlugins.contains(plugin.getName()));
        this.plugins.addAll(allPlugins);
        this.plugins.add(new DispatchClassPlugin());
    }

    private List<String> getdisabledPlugins() {
        return Optional.ofNullable(System.getProperty("dongtai.disabled.plugins"))
                .map(s -> Arrays.asList(s.split(",")))
                .orElse(null);
    }

    public ClassVisitor initial(ClassVisitor classVisitor, ClassContext context, PolicyManager policyManager) {
        Policy policy = policyManager.getPolicy();
        if (policy == null) {
            return classVisitor;
        }

        classVisitor = new DispatchHardcodedPlugin().dispatch(classVisitor, context, policy);
        for (DispatchPlugin plugin : plugins) {
            ClassVisitor pluginVisitor = plugin.dispatch(classVisitor, context, policy);
            if (pluginVisitor != classVisitor) {
                classVisitor = pluginVisitor;
                // TODO: need transform multiple times?
                if (!context.getClassName().equals(DispatchJ2ee.APACHE_COYOTE_WRITER)) {
                    break;
                }
            }
        }
        return classVisitor;
    }
}
