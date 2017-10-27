package com.hypertino.service.config

import java.io.{File, FileNotFoundException}

import com.typesafe.config.{Config, ConfigFactory, ConfigObject, ConfigValue}

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
             environment: Option[String] = None
           ): Config = {

    val defaults = if (loadDefaults)
      ConfigFactory.load()
    else
      ConfigFactory.empty()

    val config = configFiles.foldLeft(defaults)({ (conf, filePath) ⇒
      val file = new java.io.File(filePath.trim)
      if (!file.exists && failIfConfigNotFound) {
        throw new FileNotFoundException(s"${file.getAbsolutePath} is not found")
      }
      ConfigFactory.parseFile(file).withFallback(conf)
    }).resolve()

    environment.map { e ⇒
      collapseEnvironment(config, e)
    } getOrElse {
      config
    }
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
