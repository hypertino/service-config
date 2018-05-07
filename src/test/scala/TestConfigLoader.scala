/*
 * Copyright (c) 2017 Magomed Abdurakhmanov, Hypertino
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import java.io.{File, IOException}

import com.hypertino.service.config.ConfigLoader
import com.hypertino.service.config.ConfigLoader.parseConfigFilesProperty
import com.typesafe.config.ConfigException
import org.scalatest.{FreeSpec, Matchers}

class TestConfigLoader extends FreeSpec with Matchers {
  "ConfigLoader should load default config" in {
    val config = ConfigLoader()
    config.getString("test-value") shouldBe "100500"
  }

  "ConfigLoader should override config with system properties" in {
    System.setProperty("test-overridden-value", "12345")
    System.setProperty("new-value", "100500")
    val config = ConfigLoader()
    config.getString("new-value") shouldBe "100500"
    config.getString("test-overridden-value") shouldBe "12345"
  }

  "ConfigLoader should load config + command-line-files with system properties" in {
    System.setProperty("test-configs", "./testdata/test-1.conf" + File.pathSeparator + "./testdata/test-2.conf")
    val config = ConfigLoader(parseConfigFilesProperty("test-configs"))
    config.getString("test-value") shouldBe "shwonder"
    config.getString("test-name") shouldBe "Abraham"
  }

  "ConfigLoader should load config + command-line-files directly" in {
    val config = ConfigLoader(Seq("./testdata/test-1.conf", "./testdata/test-2.conf"),true,true)
    config.getString("test-value") shouldBe "shwonder"
    config.getString("test-name") shouldBe "Abraham"
  }

  "ConfigLoader should load config + command-line-resources directly" in {
    val config = ConfigLoader(Seq("resources://custom.conf"),true,true)
    config.getString("custom-value") shouldBe "John"
    config.getString("test-value") shouldBe "100500"
  }

  "ConfigLoader should fail if config isn't found with system properties" in {
    System.setProperty("test-configs", "./testdata/not-existing.conf" + File.pathSeparator + "./testdata/test-2.conf")

    intercept[ConfigException.IO] {
      ConfigLoader(parseConfigFilesProperty("test-configs"))
    }
  }

  "ConfigLoader should fail if config isn't found" in {
    intercept[ConfigException.IO] {
      ConfigLoader(Seq("./testdata/not-existing.conf"),true,true)
    }
  }

  "ConfigLoader should not load default config" in {
    val config = ConfigLoader(loadSystemProperties=false,loadDefaults=false)
    intercept[ConfigException.Missing] {
      config.getString("test-value") shouldBe "100500"
    }
  }

  "ConfigLoader should ignore environment if not set" in {
    val c = ConfigLoader(Seq("resources://application.conf"), loadSystemProperties=false,loadDefaults=false)
    c.getInt("test-env.int-value") shouldBe 100
    c.getInt("test-env.resolve") shouldBe 100
    c.getString("test-env.object-value.name") shouldBe "default"
    c.getString("test-env.object-value.removed") shouldBe "some"
    c.getString("test-env.resolve-system-properties") shouldBe "100500"
  }

  "ConfigLoader should collapse environment" in {
    val c = ConfigLoader(Seq("resources://application.conf"), loadSystemProperties=false, loadDefaults=false, environment = Some("prod"))
    c.getInt("test-env.int-value") shouldBe 20
    c.getInt("test-env.resolve") shouldBe 20
    c.getString("test-env.object-value.name") shouldBe "production"
    c.hasPath("test-env.object-value.removed") shouldBe false
    c.getString("test-env.resolve-system-properties") shouldBe "100500"
  }

  "ConfigLoader should collapse environment and resolve system properties" in {
    System.setProperty("test-overridden-value", "12345")
    val c = ConfigLoader(Seq("resources://application.conf"), loadSystemProperties=true, loadDefaults=false, environment=Some("qa"))
    c.getInt("test-env.int-value") shouldBe 200
    c.getInt("test-env.resolve") shouldBe 200
    c.getString("test-env.object-value.name") shouldBe "qa"
    c.hasPath("test-env.object-value.removed") shouldBe false
    c.getString("test-env.resolve-system-properties") shouldBe "12345"
  }

  "ConfigLoader should collapse environment with unresolved array items" in {
    System.setProperty("test-overridden-value", "12345")
    val c = ConfigLoader(Seq("resources://application.conf"), loadSystemProperties=true, loadDefaults=false, environment=Some("qa"))
    import scala.collection.JavaConverters._
    c.getConfigList("test-env.array-obj-value-resolve-without-env").asScala.head.getString("dir") shouldBe "12345"
  }

  "ConfigLoader should not fail when ConfigDelayedMerge is used" in {
    val c = ConfigLoader(Seq("resources://application.conf"), loadSystemProperties=true, loadDefaults=false, environment=Some("qa"))
    c.getInt("test-env.int-value") shouldBe 200
  }

  "ConfigLoader should not fail when keys is quoted" in {
    val c = ConfigLoader(Seq("resources://application.conf"), loadSystemProperties=false, loadDefaults=false, environment=Some("qa"))
    c.getInt("test-env.\"quoted key\"") shouldBe 2
    import scala.collection.JavaConverters._
    c.getConfigList("test-env.\"quoted key inside array\"").asScala.head.getInt("\":+\"") shouldBe 2
  }

  "ConfigLoader should collapse environment within arrays" in {
    import scala.collection.JavaConverters._
    val c1 = ConfigLoader(Seq("resources://application.conf"), loadSystemProperties=false, loadDefaults=false)
    c1.getConfigList("test-env.array-obj-value").asScala.head.getString("name") shouldBe "default"

    val c2 = ConfigLoader(Seq("resources://application.conf"), loadSystemProperties=false, loadDefaults=false, environment=Some("prod"))
    c2.getConfigList("test-env.array-obj-value").asScala.head.getString("name") shouldBe "prod"
  }

  "ConfigLoader should override fallback values" in {
    val config = ConfigLoader(Seq("resources://custom.conf"),true,true, environment=Some("qa"))
    config.getInt("fallback-overridden-value") shouldBe 2
  }
}
