# CivPlusPlugins
This repository includes all of the open-source plugins that Civ+ maintains.

Note that a file named Spigot-{version}.jar is expected in the `Spigot` folder, where {version} is whatever version is targetted.

Assuming Bazel is installed, simply run `bazel build //{Name}:all` to build a single plugin (where {Name} is the name of the plugin), or `bazel build ...` to build all plugins.
