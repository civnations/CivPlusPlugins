load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_jvm_library")

def _plugin_yml_preprocessor_impl(ctx):
  inp = ctx.attr.input.files.to_list()[0]
  out = ctx.outputs.plugin_yml_file
  ctx.actions.expand_template(
    template = inp,
    output = out,
    #output = ctx.outputs.plugin_yml_file,
    substitutions = {
      "${name}": ctx.attr.name_,
    },
  )
  print(ctx.outputs.plugin_yml_file)
  return struct(
    files = depset([ctx.outputs.plugin_yml_file])
  )

plugin_yml_preprocessor = rule(
  implementation = _plugin_yml_preprocessor_impl,
  attrs = {
    "name_": attr.string(mandatory = True),
    "input": attr.label(mandatory = True, allow_single_file = True),
  },
  outputs = {
    "plugin_yml_file": "src/main/resources/plugin.yml",
  },
)

def civ_plugin_jar(name, deps = [], resource_jars = []):  
  plugin_yml_preprocessor(
    name = "plugin_yml",
    name_ = name,
    input = "src/main/resources/template_plugin.yml",
  )

  native.java_library(
    name = name,
    srcs = native.glob(["src/main/java/**/*.java"]),
    deps = deps,
    resource_jars = resource_jars,
    resources = native.glob(
      ["src/main/resources/**"],
      exclude = ["src/main/resources/template_plugin.yml"],
    ) + [":plugin_yml"],
    visibility = ["//visibility:public"]
  )

  native.genrule(
     name = "rename_jar",
     srcs = ["lib{}.jar".format(name)],
     outs = ["{}.jar".format(name)],
     cmd = "cp $< $@",
  )

def civ_plugin_kt_jar(name, deps = []):
  plugin_yml_preprocessor(
    name = "plugin_yml",
    name_ = name,
    input = "src/main/resources/template_plugin.yml",
  )
  
  kt_name = name + "_kt"
  kt_jvm_library(
    name = kt_name,
    srcs = native.glob(["src/main/kotlin/**/*.kt"]),
    deps = deps,
    visibility = ["//visibility:public"],
  )
  
  native.java_library(
    name = name,
    srcs = native.glob(["src/main/java/**/*.java"]),
    runtime_deps = ["@com_github_jetbrains_kotlin//:kotlin-stdlib"],
    resource_jars = [":" + kt_name, "@com_github_jetbrains_kotlin//:kotlin-stdlib"],
    resources = native.glob(
      ["src/main/resources/**"],
      exclude = ["src/main/resources/template_plugin.yml"],
    ) + [":plugin_yml"],
    visibility = ["//visibility:public"]
  )

  native.genrule(
     name = "rename_jar",
     srcs = ["lib{}.jar".format(name)],
     outs = ["{}.jar".format(name)],
     cmd = "cp $< $@",
   )
