load("//:civ_build.bzl", "civ_plugin_jar")

civ_plugin_jar(
    name = "CombatTagPlus",
    version = "1.4.0",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = [
      "//Spigot:Spigot",
      "@maven//:me_confuser_BarAPI",
      "@maven//:com_connorlinfoot_ActionBarAPI",
      "@maven//:org_inventivetalent_BossBarAPI",
    ],
)
