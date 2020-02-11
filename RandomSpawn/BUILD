load("//:civ_build.bzl", "civ_plugin_jar")

civ_plugin_jar(
    name = "RandomSpawn",
    version = "2.9.0",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "//CivModCore:CivModCore",
      "//Bastion:Bastion",
      "//WorldBorder:WorldBorder",
    ],
)
