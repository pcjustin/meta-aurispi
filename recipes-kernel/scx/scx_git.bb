SUMMARY = "sched_ext schedulers and tools"
DESCRIPTION = "A Linux kernel feature which enables implementing kernel thread schedulers in BPF and dynamically loading them."
HOMEPAGE = "https://github.com/sched-ext/scx"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b234ee4d69f5fce4486a80fdaf4a4263"

# Use v1.0.19 release tag
SRCREV = "v1.0.19"
PV = "1.0.19"

SRC_URI = "git://github.com/sched-ext/scx.git;protocol=https;branch=main \
"

S = "${UNPACKDIR}/scx-1.0.19"

# Inherit cargo for Rust build support
inherit cargo cargo-update-recipe-crates ptest-cargo

# Build dependencies
DEPENDS = " \
    libbpf \
    elfutils \
    zlib \
    zstd \
    clang-native \
    bpftool-native \
    glibc \
"

EXCLUDE_FROM_WORLD = "1"

require ${BPN}-crates.inc
