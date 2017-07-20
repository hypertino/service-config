package com.hypertino.service.config

import java.io.File

import com.typesafe.config.Config
import scaldi.{Injector, Module}

class ConfigModule(localConfigPropertyName: String,
                   separator: String,
                   failIfConfigNotFound: Boolean,
                   loadDefaults: Boolean) extends Module {
  val rootConfig = ConfigLoader(localConfigPropertyName, separator, failIfConfigNotFound, loadDefaults)
  bind[Config] identifiedBy 'config toNonLazy rootConfig
}

object ConfigModule {
  def apply(localConfigPropertyName: String = "config.localfile",
            separator: String = File.pathSeparator,
            failIfConfigNotFound: Boolean = true,
            loadDefaults: Boolean = true,
            injectModulesConfigPath: Option[String] = Some("inject-modules")): Injector = {

    val configModule = new ConfigModule(localConfigPropertyName, separator, failIfConfigNotFound, loadDefaults)
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
