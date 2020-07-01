load("//:civ_build.bzl", "civ_plugin_jar")
load("@com_github_johnynek_bazel_jar_jar//:jar_jar.bzl", "jar_jar")

name = "CivModCore"
version = "1.9.0"

java_binary(
	name = "shade_bin",
	main_class = "none",
	runtime_deps = [
		"@maven//:com_zaxxer_HikariCP",
		"@maven//:org_slf4j_slf4j_api",
		"@maven//:co_aikar_acf_bukkit",
	]
)

jar_jar(
    name = "shade_fat",
    input_jar = ":shade_bin_deploy.jar",
    rules = "RULES"
)

#jar_jar(
#    name = "shade_slf4j",
#    input_jar = "@maven//:org_slf4j_slf4j_api",
#    rules = "RULES"
#)

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
	resource_jars = [":shade_fat"]
)
