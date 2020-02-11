load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_jvm_library")

def civ_plugin_jar(name, version, srcs, resources = [], deps = []):
  native.java_library(name = name, srcs = srcs, deps = deps, resources = resources, visibility = ["//visibility:public"])
  native.genrule(
     name = "civ_plugin_jar_rename",
     outs = ["{}-{}.jar".format(name, version)],
     srcs = ["lib{}.jar".format(name)],
     cmd = "cp $< $@",
  )

def civ_plugin_kt_jar(name, version, srcs, resources = [], deps = []):
  kt_jvm_library(name = name, srcs = srcs, deps = deps, resources = resources, visibility = ["//visibility:public"])
  native.genrule(
     name = "civ_plugin_kt_jar_rename",
     outs = ["{}-{}.jar".format(name, version)],
     srcs = ["{}.jar".format(name)],
     cmd = "cp $< $@",
  )
