import com.hypertino.service.config.{ConfigLoader, ConfigModule}
import com.typesafe.config.Config
import org.scalatest.{FreeSpec, Matchers}
import scaldi.Injectable

class TestConfigModule extends FreeSpec with Matchers with Injectable {
  "ConfigModule should bind config" in {
    implicit val injector = ConfigModule()

    val config = inject[Config]
    config.getString("test-value") shouldBe "100500"
  }

  "ConfigModule should inject module from config" in {
    implicit val injector = ConfigModule()

    val testString = inject[String](identified by 'testString)
    testString shouldBe "Shaitan"
  }
}
