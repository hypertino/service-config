import scaldi.Module

class TestInjector extends Module {
  bind[String] identifiedBy 'testString toNonLazy "Shaitan"
}
