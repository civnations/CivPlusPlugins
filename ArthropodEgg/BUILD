load("//:civ_build.bzl", "civ_plugin_jar")

civ_plugin_jar(
    name = "ArthropodEgg",
    version = "1.7.0",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "//CivModCore:CivModCore",
    ],
)
