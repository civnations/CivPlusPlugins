load("//:civ_build.bzl", "civ_plugin_jar")

name = "CivModCore"
version = "1.8.0"
deps = [
  "//Spigot:Spigot",
  "@maven//:com_zaxxer_HikariCP",
  "@maven//:org_slf4j_slf4j_api",
]

civ_plugin_jar(
	name = name,
	version = version,
	deps = deps,
)
