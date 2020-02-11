load("//:civ_build.bzl", "civ_plugin_jar")

civ_plugin_jar(
    name = "BanStick",
    version = "1.2.0",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "//CivModCore:CivModCore",
      "//NameLayer:NameLayer",
      "@maven//:com_github_seancfoley_ipaddress",
      "@maven//:org_jsoup_jsoup",
    ],
)
