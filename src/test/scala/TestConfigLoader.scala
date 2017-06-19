import java.io.{File, IOException}

import com.hypertino.service.config.ConfigLoader
import com.typesafe.config.ConfigException
import org.scalatest.{FreeSpec, Matchers}

class TestConfigLoader extends FreeSpec with Matchers {
  "ConfigLoader should load default config" in {
    val config = ConfigLoader()
    config.getString("test-value") shouldBe "100500"
  }

  "ConfigLoader should load config + command-line-files with system properties" in {
    System.setProperty("test-configs", "./testdata/test-1.conf" + File.pathSeparator + "./testdata/test-2.conf")
    val config = ConfigLoader(localConfigPropertyName = "test-configs")
    config.getString("test-value") shouldBe "shwonder"
    config.getString("test-name") shouldBe "Abraham"
  }

  "ConfigLoader should load config + command-line-files directly" in {
    val config = ConfigLoader(Seq("./testdata/test-1.conf", "./testdata/test-2.conf"),true,true)
    config.getString("test-value") shouldBe "shwonder"
    config.getString("test-name") shouldBe "Abraham"
  }

  "ConfigLoader should fail if config isn't found with system properties" in {
    System.setProperty("test-configs", "./testdata/not-existing.conf" + File.pathSeparator + "./testdata/test-2.conf")

    intercept[IOException] {
      ConfigLoader(localConfigPropertyName = "test-configs")
    }
  }

  "ConfigLoader should fail if config isn't found" in {
    intercept[IOException] {
      ConfigLoader(Seq("./testdata/not-existing.conf"),true,true)
    }
  }

  "ConfigLoader should not load default config" in {
    val config = ConfigLoader(loadDefaults=false)
    intercept[ConfigException.Missing] {
      config.getString("test-value") shouldBe "100500"
    }
  }
}
