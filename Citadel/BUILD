load("//:civ_build.bzl", "civ_plugin_jar")

civ_plugin_jar(
    name = "Citadel",
    version = "4.1.0",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "//CivModCore:CivModCore",
      "//NameLayer:NameLayer",
      "@maven//:com_gmail_filoghost_holographicdisplays_holographicdisplays_api",
    ],
)
