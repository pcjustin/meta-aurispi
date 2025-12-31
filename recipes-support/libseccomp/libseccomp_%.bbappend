# Enable native build variant for libseccomp
BBCLASSEXTEND = "native"

# Override REQUIRED_DISTRO_FEATURES to allow building without seccomp feature
REQUIRED_DISTRO_FEATURES:class-native = ""
