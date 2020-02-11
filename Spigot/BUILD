package(default_visibility = ["//visibility:public"])

load("//:vars.bzl", "SPIGOT_VERSION")

#command = "echo PWDDIR$$PWD && export HOME=$$PWD && java -jar $(location @SpigotBuildTools//file) --rev {} > $@".format(SPIGOT_VERSION)
#genrule(
#    name = "Spigot",
#    srcs = ["@SpigotBuildTools//file"],
#    cmd = command,
#    outs = ["spigot-{}.jar".format(SPIGOT_VERSION)],
#)

java_import(
    name = "Spigot",
    jars = [
        "Spigot-{}.jar".format(SPIGOT_VERSION),
    ],
)
