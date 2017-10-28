/*
 * Copyright (c) 2017 Magomed Abdurakhmanov, Hypertino
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.hypertino.service.config

import com.hypertino.service.config.ConfigLoader.parseConfigFilesProperty
import com.typesafe.config.Config
import scaldi.{Injector, Module}

class ConfigModule(configFiles: Seq[String],
                   failIfConfigNotFound: Boolean,
                   loadDefaults: Boolean,
                   loadSystemProperties: Boolean,
                   environment: Option[String]) extends Module {
  val rootConfig = ConfigLoader(configFiles, failIfConfigNotFound, loadDefaults, loadSystemProperties, environment)
  bind[Config] identifiedBy 'config toNonLazy rootConfig
}

object ConfigModule {
  def apply(configFiles: Seq[String] = parseConfigFilesProperty(),
            failIfConfigNotFound: Boolean = true,
            loadDefaults: Boolean = true,
            loadSystemProperties: Boolean = true,
            environment: Option[String] = None,
            injectModulesConfigPath: Option[String] = Some("inject-modules")): Injector = {

    val configModule = new ConfigModule(configFiles, failIfConfigNotFound, loadDefaults, loadSystemProperties, environment)
    loadConfigInjectedModules(configModule.rootConfig, injectModulesConfigPath, configModule)
  }

  private def loadConfigInjectedModules(config: Config, configPath: Option[String], previous: Injector): Injector = {
    import scala.collection.JavaConverters._
    if (configPath.isDefined && config.hasPath(configPath.get)) {
      var module = previous
      config.getStringList(configPath.get).asScala.foreach { injectModuleClassName ⇒
        module = module :: Class.forName(injectModuleClassName).newInstance().asInstanceOf[Injector]
      }
      module
    } else {
      previous
    }
  }
}
