load("//:civ_build.bzl", "civ_plugin_jar")

civ_plugin_jar(
    name = "WorldBorder",
    version = "1.10.0",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "@maven//:us_dynmap_dynmap_api",
      "@maven//:io_papermc_paperlib",
    ],
)
