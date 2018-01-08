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
    val config = ConfigLoader(loadDefaults=false)
    intercept[ConfigException.Missing] {
      config.getString("test-value") shouldBe "100500"
    }
  }

  "ConfigLoader should collapse environment" in {
    System.clearProperty("test-overridden-value")
    val c1 = ConfigLoader()
    c1.getInt("test-env.int-value") shouldBe 100
    c1.getInt("test-env.resolve") shouldBe 100
    c1.getString("test-env.object-value.name") shouldBe "default"
    c1.getString("test-env.object-value.removed") shouldBe "some"
    c1.getString("test-env.resolve-system-properties") shouldBe "100500"

    val c2 = ConfigLoader(environment=Some("prod"))
    c2.getInt("test-env.int-value") shouldBe 20
    c2.getInt("test-env.resolve") shouldBe 20
    c2.getString("test-env.object-value.name") shouldBe "production"
    c2.hasPath("test-env.object-value.removed") shouldBe false
    c2.getString("test-env.resolve-system-properties") shouldBe "100500"

    System.setProperty("test-overridden-value", "12345")
    val c3 = ConfigLoader(environment=Some("qa"))
    c3.getInt("test-env.int-value") shouldBe 200
    c3.getInt("test-env.resolve") shouldBe 200
    c3.getString("test-env.object-value.name") shouldBe "qa"
    c3.hasPath("test-env.object-value.removed") shouldBe false
    c3.getString("test-env.resolve-system-properties") shouldBe "12345"
  }

  "ConfigLoader should collapse environment within arrays" in {
    import scala.collection.JavaConverters._
    System.clearProperty("test-overridden-value")
    val c1 = ConfigLoader()
    c1.getConfigList("test-env.array-obj-value").asScala.head.getString("name") shouldBe "default"

    val c2 = ConfigLoader(environment=Some("prod"))
    c2.getConfigList("test-env.array-obj-value").asScala.head.getString("name") shouldBe "prod"
  }
}
