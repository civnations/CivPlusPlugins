load("//:civ_build.bzl", "civ_plugin_kt_jar")

civ_plugin_kt_jar(
    name = "Bumhug",
    version = "1.1.0",
    srcs = glob(["src/main/kotlin/**/*.kt"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "//CivModCore:CivModCore",
      "//Citadel:Citadel",
      "//CombatTagPlus:CombatTagPlus",
      "@maven//:com_comphenix_protocol_ProtocolLib",
    ],
)
