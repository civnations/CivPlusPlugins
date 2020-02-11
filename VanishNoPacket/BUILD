load("//:civ_build.bzl", "civ_plugin_jar")

civ_plugin_jar(
    name = "VanishNoPacket",
    version = "3.20.0",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "@maven//:com_comphenix_protocol_ProtocolLib",
    ],
)
