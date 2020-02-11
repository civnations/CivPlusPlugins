workspace(name = "CivPlusPlugins")

#load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_file")
#http_file(
#  name = "SpigotBuildTools",
#  urls = ["https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar"],
#  executable = True
#)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
RULES_JVM_EXTERNAL_TAG = "3.0"
RULES_JVM_EXTERNAL_SHA = "62133c125bf4109dfd9d2af64830208356ce4ef8b165a6ef15bbff7460b35c3a"
http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)
load("@rules_jvm_external//:defs.bzl", "maven_install")
maven_install(
    artifacts = [
      "com.zaxxer:HikariCP:3.3.1",
      "org.slf4j:slf4j-api:1.7.26",
      "com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.3.2",
      "us.dynmap:dynmap-api:2.5",
      "io.papermc:paperlib:1.0.2",
      "org.mockito:mockito-core:2.21.0",
      "com.comphenix.protocol:ProtocolLib:4.5.0",
      "org.apache.logging.log4j:log4j-core:2.7",
      "com.github.seancfoley:ipaddress:2.0.1",
      "org.jsoup:jsoup:1.10.2",

    ],
    repositories = [
      "https://jcenter.bintray.com/",
      "https://repo1.maven.org/maven2",
      # Devoted Build Server
      "https://build.devotedmc.com/plugin/repository/everything/",
      # Holographic Displays API (used by Citadel)
      "https://ci.filoghost.me/plugin/repository/everything/",
      # DynMap (used by WorldBorder)
      "https://repo.mikeprimm.com/",
      # PaperLib (used by WorldBorder)
      "https://papermc.io/repo/repository/maven-public/",
      # ProtocolLib
      "https://repo.dmulloy2.net/nexus/repository/public/",
    ],
    fail_on_missing_checksum = False,
)

local_repository(
  name = "Spigot",
  path = __workspace_dir__ + "/Spigot/",
)

local_repository(
  name = "CivModCore",
  path = __workspace_dir__ + "/CivModCore/",
)
