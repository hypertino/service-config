/*
 * Copyright (c) 2017 Magomed Abdurakhmanov, Hypertino
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.hypertino.service.config
import java.io.File
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

    // systemProperties has top-priority to be able to override everything later
    val systemProperties = if (loadSystemProperties)
      ConfigFactory.parseProperties(System.getProperties)
    else
      ConfigFactory.empty()

    val defaults = if (loadDefaults)
    // we don't use ConfigFactory.load because it always immediately resolves substitutions in application.conf
      ConfigFactory.parseResources("application.conf").withFallback(ConfigFactory.defaultReference)
    else
      ConfigFactory.empty()

    val config = systemProperties.withFallback(configFiles.foldLeft(defaults)({ (conf, path) ⇒
      if (path.startsWith("resources://")) {
        ConfigFactory.parseResources(path.substring("resources://".length),
          ConfigParseOptions.defaults().setAllowMissing(!failIfConfigNotFound)).withFallback(conf)
      }
      else {
        val file = new java.io.File(path.trim)
        ConfigFactory.parseFile(file, ConfigParseOptions.defaults().setAllowMissing(!failIfConfigNotFound)).withFallback(conf)
      }
    }))

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
      case (key, v: ConfigList) =>
        Seq((if (path.isEmpty) key else path + "." + key,
          ConfigValueFactory.fromIterable(
            v.asScala.map{
              case o: ConfigObject => substituteArrayElement(o, envSuffix).unwrapped()
              case other => other.unwrapped()
            }.asJava
          )))
      case _ ⇒ Seq.empty
    }
  }

  // sadly this doesn't work without resolving, we only use on array elements
  private def substituteArrayElement(configObject: ConfigObject, envSuffix: String): ConfigObject = {
    import scala.collection.JavaConverters._
    val objectMap = configObject.asScala.map {
      case (key, v: ConfigObject) ⇒ key -> substituteArrayElement(v, envSuffix).unwrapped()
      case (key, v: ConfigList) ⇒ key -> v.asScala.map {
        case iv: ConfigObject => substituteArrayElement(iv, envSuffix).unwrapped()
        case other => other.unwrapped()
      }
      case (key,v) => key -> v.unwrapped()
    }
    val objectMapNew = objectMap.filter(_._1.endsWith(envSuffix)).map { case (key, v) =>
      key.substring(0, key.length - envSuffix.length) -> v
    }

    ConfigValueFactory.fromMap((objectMap ++ objectMapNew).asJava)
  }
}
