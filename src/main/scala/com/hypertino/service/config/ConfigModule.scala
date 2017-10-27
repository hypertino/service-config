package com.hypertino.service.config

import com.hypertino.service.config.ConfigLoader.parseConfigFilesProperty
import com.typesafe.config.Config
import scaldi.{Injector, Module}

class ConfigModule(configFiles: Seq[String],
                   failIfConfigNotFound: Boolean,
                   loadDefaults: Boolean) extends Module {
  val rootConfig = ConfigLoader(configFiles, failIfConfigNotFound, loadDefaults)
  bind[Config] identifiedBy 'config toNonLazy rootConfig
}

object ConfigModule {
  def apply(configFiles: Seq[String] = parseConfigFilesProperty(),
            failIfConfigNotFound: Boolean = true,
            loadDefaults: Boolean = true,
            injectModulesConfigPath: Option[String] = Some("inject-modules")): Injector = {

    val configModule = new ConfigModule(configFiles, failIfConfigNotFound, loadDefaults)
    loadConfigInjectedModules(configModule.rootConfig, injectModulesConfigPath, configModule)
  }

  private def loadConfigInjectedModules(config: Config, configPath: Option[String], previous: Injector): Injector = {
    import scala.collection.JavaConversions._
    if (configPath.isDefined && config.hasPath(configPath.get)) {
      var module = previous
      config.getStringList(configPath.get).foreach { injectModuleClassName ⇒
        module = module :: Class.forName(injectModuleClassName).newInstance().asInstanceOf[Injector]
      }
      module
    } else {
      previous
    }
  }
}
