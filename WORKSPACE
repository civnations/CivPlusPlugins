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

# Makes Kotlin work
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
rules_kotlin_version = "legacy-1.3.0"
rules_kotlin_sha = "4fd769fb0db5d3c6240df8a9500515775101964eebdf85a3f9f0511130885fde"
http_archive(
    name = "io_bazel_rules_kotlin",
    urls = ["https://github.com/bazelbuild/rules_kotlin/archive/%s.zip" % rules_kotlin_version],
    type = "zip",
    strip_prefix = "rules_kotlin-%s" % rules_kotlin_version,
    sha256 = rules_kotlin_sha,
)
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")
kotlin_repositories()
kt_register_toolchains()
