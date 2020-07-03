load("//:civ_build.bzl", "civ_plugin_jar")
load("@com_github_johnynek_bazel_jar_jar//:jar_jar.bzl", "jar_jar")

name = "BanStick"

java_binary(
	name = "shade_bin",
	main_class = "none",
	runtime_deps = [
		"@maven//:com_github_seancfoley_ipaddress",
	]
)

jar_jar(
    name = "shade_fat",
    input_jar = ":shade_bin_deploy.jar",
    rules = "RULES"
)

deps = [
  "//Spigot:Spigot",
  "//CivModCore:CivModCore",
  "//NameLayer:NameLayer",
  "@maven//:com_github_seancfoley_ipaddress",
  "@maven//:org_jsoup_jsoup",
]

civ_plugin_jar(
	name = name,
	deps = deps,
	resource_jars = [":shade_fat"]
)
