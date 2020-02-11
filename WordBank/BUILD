load("//:civ_build.bzl", "civ_plugin_jar")

civ_plugin_jar(
    name = "WordBank",
    version = "1.3.0",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "@maven//:com_zaxxer_HikariCP",
      "@maven//:org_mockito_mockito_core",
    ],
)
