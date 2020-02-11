load("//:civ_build.bzl", "civ_plugin_jar")

civ_plugin_jar(
    name = "Finale",
    version = "1.5.0",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "//CivModCore:CivModCore",
	  "//CombatTagPlus:CombatTagPlus",
      "@maven//:com_comphenix_protocol_ProtocolLib",
    ],
)
