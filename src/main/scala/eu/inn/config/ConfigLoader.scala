package eu.inn.config

import java.io.File

import com.typesafe.config.ConfigFactory

class ConfigLoader(localConfigPropertyName: String = "config.localfile", separator: String = File.pathSeparator) {
  def load() = {
    val conf = System.getProperty(localConfigPropertyName, "")
      .split(separator)
      .foldLeft(ConfigFactory.load())({ (conf, filePath) ⇒
        val file = new java.io.File(filePath.trim)
        ConfigFactory.parseFile(file).withFallback(conf)
      })
      .resolve()
  }
}
