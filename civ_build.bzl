def civ_plugin_jar(name, version, srcs, resources = [], deps = []):
  native.java_library(name = name, srcs = srcs, deps = deps, resources = resources, visibility = ["//visibility:public"])
  native.genrule(
     name = "civ_plugin_jar",
     outs = ["{}-{}.jar".format(name, version)],
     srcs = ["lib{}.jar".format(name)],
     cmd = "cp $< $@",
  )
