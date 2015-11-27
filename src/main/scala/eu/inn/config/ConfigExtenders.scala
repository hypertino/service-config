package eu.inn.config

import java.util.concurrent.TimeUnit

import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration

object ConfigExtenders {
  implicit class ExtendConfig(config: Config) {
    def getOptionString(path: String) = opt(path, _.getString)
    def getString(path: String, default: String) = opt(path, _.getString).getOrElse(default)
    def getOptionBoolean(path: String) = opt(path, _.getBoolean)
    def getBoolean(path: String, default: Boolean) = opt(path, _.getBoolean).getOrElse(default)
    def getOptionInt(path: String) = opt(path, _.getInt)
    def getInt(path: String, default: Int) = opt(path, _.getInt).getOrElse(default)
    def getOptionLong(path: String) = opt(path, _.getLong)
    def getLong(path: String, default: Long) = opt(path, _.getLong).getOrElse(default)

    def getFiniteDuration(path: String) = FiniteDuration(
      config.getDuration(path, TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS
    )

    def getOptionFiniteDuration(path: String) = if (config.hasPath(path)) {
      FiniteDuration(
        config.getDuration(path, TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS
      )
    } else {
      None
    }

    def getFiniteDuration(path: String, default: FiniteDuration) = if (config.hasPath(path)) {
      FiniteDuration(
        config.getDuration(path, TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS
      )
    } else {
      default
    }

    def getOptionConfig(path: String) = opt(path, _.getConfig)
    def getOptionObject(path: String) = opt(path, _.getObject)
    def getOptionConfigList(path: String) = opt(path, _.getConfigList)
    def getOptionStringList(path: String) = opt(path, _.getStringList)

    private def opt[T](path: String, getter: Config ⇒ String ⇒ T): Option[T] =
      if (config.hasPath(path)) {
        Option(getter(config)(path))
      } else {
        None
      }
  }
}
