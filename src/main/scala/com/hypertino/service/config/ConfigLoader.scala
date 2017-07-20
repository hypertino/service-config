package com.hypertino.service.config

import java.io.{File, FileNotFoundException}

import com.typesafe.config.{Config, ConfigFactory}

object ConfigLoader {
  def parseConfigProperty(localConfigPropertyName: String = "config.localfile",
            separator: String = File.pathSeparator): Seq[String] = {
    System.getProperty(localConfigPropertyName, "")
      .split(separator)
      .filterNot(_.trim.isEmpty)
  }

  def apply(configFiles: Seq[String] = parseConfigProperty(),
            failIfConfigNotFound: Boolean = true,
            loadDefaults: Boolean = true): Config = {

    val defaults = if(loadDefaults)
      ConfigFactory.load()
    else
      ConfigFactory.empty()

    configFiles.foldLeft(defaults)({ (conf, filePath) ⇒
      val file = new java.io.File(filePath.trim)
      if (!file.exists && failIfConfigNotFound) {
        throw new FileNotFoundException(s"${file.getAbsolutePath} is not found")
      }
      ConfigFactory.parseFile(file).withFallback(conf)
    }).resolve()
  }
}
