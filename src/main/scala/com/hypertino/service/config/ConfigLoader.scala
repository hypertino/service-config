/*
 * Copyright (c) 2017 Magomed Abdurakhmanov, Hypertino
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.hypertino.service.config

import java.io.{File, FileNotFoundException}
import java.util.Properties

import com.typesafe.config._

object ConfigLoader {
  def parseConfigFilesProperty(configFilesPropertyName: String = "config-files",
                               separator: String = File.pathSeparator): Seq[String] = {
    System.getProperty(configFilesPropertyName, "")
      .split(separator)
      .filterNot(_.trim.isEmpty)
  }

  def apply(
             configFiles: Seq[String] = parseConfigFilesProperty(),
             failIfConfigNotFound: Boolean = true,
             loadDefaults: Boolean = true,
             loadSystemProperties: Boolean = true,
             environment: Option[String] = None
           ): Config = {

    val systemProperties = if (loadSystemProperties)
      ConfigFactory.parseProperties(System.getProperties)
    else
      ConfigFactory.empty()

    val defaults = if (loadDefaults)
      // we don't use ConfigFactory.load because it always immediately resolves substitutions in application.conf
      systemProperties
        .withFallback(ConfigFactory.parseResources("application.conf"))
        .withFallback(ConfigFactory.defaultReference)
    else
      systemProperties

    val config = configFiles.foldLeft(defaults)({ (conf, path) ⇒
      if (path.startsWith("resources://")) {
        ConfigFactory.parseResources(path.substring("resources://".length),
          ConfigParseOptions.defaults().setAllowMissing(!failIfConfigNotFound)).withFallback(conf)
      }
      else {
        val file = new java.io.File(path.trim)
//        if (!file.exists && failIfConfigNotFound) {
//          throw new FileNotFoundException(s"${file.getAbsolutePath} is not found")
//        }
        ConfigFactory.parseFile(file, ConfigParseOptions.defaults().setAllowMissing(!failIfConfigNotFound)).withFallback(conf)
      }
    })

    environment.map { e ⇒
      collapseEnvironment(config, e)
    } getOrElse {
      config
    } resolve()
  }

  def collapseEnvironment(config: Config, environment: String): Config = {
    substitutions("", config, "~" + environment)
      .foldLeft(config) { (latest, i) ⇒
        latest.withValue(i._1, i._2)
      }
  }

  private def substitutions(path: String, config: Config, envSuffix: String): Seq[(String,ConfigValue)] = {
    import scala.collection.JavaConverters._
    config.root().asScala.toSeq.flatMap {
      case (key,v) if key.endsWith(envSuffix) ⇒
        Seq((if (path.isEmpty) "" else path + ".") + key.substring(0, key.length - envSuffix.length) -> v)
      case (key, v: ConfigObject) ⇒
        substitutions(if (path.isEmpty) key else path + "." + key, v.toConfig, envSuffix)
      case _ ⇒ Seq.empty
    }
  }
}
