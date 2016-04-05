package eu.inn.config

import java.io.{File, IOException}

import com.typesafe.config.{Config, ConfigFactory}

class ConfigLoader(localConfigPropertyName: String, separator: String, failIfConfigNotFound: Boolean) {
  def load(): Config = {
    System.getProperty(localConfigPropertyName, "")
      .split(separator)
      .filterNot(_.trim.isEmpty)
      .foldLeft(ConfigFactory.load())({ (conf, filePath) ⇒
        val file = new java.io.File(filePath.trim)
        if (!file.exists && failIfConfigNotFound) {
          throw new IOException(s"${file.getAbsolutePath} is not found")
        }
        ConfigFactory.parseFile(file).withFallback(conf)
      })
      .resolve()
  }
}

object ConfigLoader {
  def apply(localConfigPropertyName: String = "config.localfile",
            separator: String = File.pathSeparator,
            failIfConfigNotFound: Boolean = true): Config = {
    val loader = new ConfigLoader(localConfigPropertyName, separator, failIfConfigNotFound)
    loader.load()
  }
}
