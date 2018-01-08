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
    val sb = new StringBuilder
    renderSubstitutedConfig(config.root(), "~" + environment, sb)
    ConfigFactory.parseString(sb.toString, ConfigParseOptions.defaults())
  }

  private val jsonRenderOptions = ConfigRenderOptions.concise().setJson(true)

  private def renderSubstitutedConfig(configObject: ConfigObject, envSuffix: String, to: StringBuilder): Unit = {
    import scala.collection.JavaConverters._
    val m = configObject.asScala
    to.append("{")
    withCommas(m, to) {
      case (key, v) if !key.endsWith(envSuffix) && m.contains(key + envSuffix) =>
        to.append(ConfigUtil.quoteString(key))
        to.append(":")
        to.append(m(key+envSuffix).render(jsonRenderOptions))

      case (key, v: ConfigObject) ⇒
        to.append(ConfigUtil.quoteString(key))
        to.append(":")
        renderSubstitutedConfig(v, envSuffix, to)

      case (key, v: ConfigList) ⇒
        to.append(ConfigUtil.quoteString(key))
        to.append(":")
        to.append("[")
        withCommas(v.asScala, to) {
          case iv: ConfigObject =>
            renderSubstitutedConfig(iv, envSuffix, to)
          case other =>
            to.append(other.render(jsonRenderOptions))
        }
        to.append("]")

      case (key, v) =>
        to.append(ConfigUtil.quoteString(key))
        to.append(":")
        to.append(v.render(jsonRenderOptions))
    }
    to.append("}")
  }

  private def withCommas[T](iterable: Iterable[T], to: StringBuilder)(code: (T) => Unit): Unit = {
    var comma = false
    iterable.foreach { i =>
      if (comma) {
        to.append(",")
      }
      comma = true
      code(i)
    }
  }
}
