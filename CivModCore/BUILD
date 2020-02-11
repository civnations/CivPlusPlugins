load("//:civ_build.bzl", "civ_plugin_jar")

civ_plugin_jar(
    name = "CivModCore",
    version = "1.8.0",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "@maven//:com_zaxxer_HikariCP",
      "@maven//:org_slf4j_slf4j_api",
    ],
)
