load("//:civ_build.bzl", "civ_plugin_jar")

name = "CivModCore"
version = "1.9.0"
deps = [
  "//Spigot:Spigot",
  "@maven//:com_zaxxer_HikariCP",
  "@maven//:org_slf4j_slf4j_api",
  "@maven//:co_aikar_acf_bukkit",
]

civ_plugin_jar(
	name = name,
	version = version,
	deps = deps,
)
