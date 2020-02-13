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
        "${version}": ctx.attr.version,
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
    "version": attr.string(mandatory = True),
    "input": attr.label(mandatory = True, allow_single_file = True),
  },
  outputs = {
    "plugin_yml_file": "src/main/resources/plugin.yml",
  },
)

def civ_plugin_jar(name, version, deps = []):  
  plugin_yml_preprocessor(
    name = "plugin_yml",
    name_ = name,
    version = version,
    input = "src/main/resources/template_plugin.yml",
  )

  native.java_library(
    name = name,
    srcs = native.glob(["src/main/java/**/*.java"]),
    deps = deps,
    resources = native.glob(
      ["src/main/resources/**"],
      exclude = ["src/main/resources/template_plugin.yml"],
    ) + [":plugin_yml"],
    visibility = ["//visibility:public"]
  )

  native.genrule(
     name = "rename_jar",
     srcs = ["lib{}.jar".format(name)],
     outs = ["{}-{}.jar".format(name, version)],
     cmd = "cp $< $@",
  )

def civ_plugin_kt_jar(name, version, deps = []):
  plugin_yml_preprocessor(
    name = "plugin_yml",
    name_ = name,
    version = version,
    input = "src/main/resources/template_plugin.yml",
  )
  
  kt_jvm_library(
    name = name,
    srcs = native.glob(["src/main/kotlin/**/*.kt"]),
    deps = deps,
    resources = native.glob(
      ["src/main/resources/**"],
      exclude = ["src/main/resources/template_plugin.yml"],
    ) + [":plugin_yml"],
    visibility = ["//visibility:public"]
  )
  
  native.genrule(
     name = "rename_jar",
     outs = ["{}-{}.jar".format(name, version)],
     srcs = ["{}.jar".format(name)],
     cmd = "cp $< $@",
  )
