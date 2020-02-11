load("//:civ_build.bzl", "civ_plugin_jar")

civ_plugin_jar(
    name = "JukeAlert",
    version = "2.1.0",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "//CivModCore:CivModCore",
      "@maven//:com_comphenix_protocol_ProtocolLib",
      
    ],
)
