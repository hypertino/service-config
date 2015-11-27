package eu.inn.config

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

class ConfigLoader(localConfigPropertyName: String, separator: String) {
  def load(): Config = {
    System.getProperty(localConfigPropertyName, "")
      .split(separator)
      .foldLeft(ConfigFactory.load())({ (conf, filePath) ⇒
        val file = new java.io.File(filePath.trim)
        ConfigFactory.parseFile(file).withFallback(conf)
      })
      .resolve()
  }
}

object ConfigLoader {
  def apply(localConfigPropertyName: String = "config.localfile", separator: String = File.pathSeparator): Config = {
    val loader = new ConfigLoader(localConfigPropertyName, separator)
    loader.load()
  }
}
