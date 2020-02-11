load("//:civ_build.bzl", "civ_plugin_jar")

civ_plugin_jar(
    name = "Bastion",
    version = "2.4.0",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "//CivModCore:CivModCore",
      "//NameLayer:NameLayer",
      "//Citadel:Citadel",
    ],
)
