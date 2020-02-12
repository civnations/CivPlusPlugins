load("//:civ_build.bzl", "civ_plugin_jar")

civ_plugin_jar(
    name = "CivChat2",
    version = "1.8.0",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "//CivModCore:CivModCore",
      "//NameLayer:NameLayer",
      "@maven//:org_apache_logging_log4j_log4j_core",
    ],
)
