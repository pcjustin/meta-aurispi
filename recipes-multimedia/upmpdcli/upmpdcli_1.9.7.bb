SUMMARY = "uPnP Media Renderer front-end for MPD"
DESCRIPTION = "upmpdcli is a renderer front-end to MPD that makes it appear as a UPnP/DLNA device"
HOMEPAGE = "http://www.lesbonscomptes.com/upmpdcli/"
LICENSE = "LGPL-2.1-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=4fbd65380cdd255951079008b364516c"

DEPENDS = "libupnpp mpd libmpdclient util-linux curl expat jsoncpp pkgconfig-native"
RDEPENDS:${PN} = "libupnpp mpd libmpdclient util-linux curl expat jsoncpp python3"

inherit useradd

USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} = "-r -s /bin/false -d /nonexistent upmpdcli"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = "https://www.lesbonscomptes.com/upmpdcli/downloads/upmpdcli-${PV}.tar.gz \
           file://upmpdcli.conf \
           file://0001-Remove-UPnP-AV-suffix-from-AV-device-friendly-name-i.patch \
           file://icon.png \
           "
SRC_URI[sha256sum] = "7b10cc35cae9377c542aba3983dfd39e34b4dd5fff64512f54feb87e64cf8609"

S = "${UNPACKDIR}/upmpdcli-${PV}"

inherit meson systemd

SYSTEMD_SERVICE:${PN} = "upmpdcli.service"
SYSTEMD_AUTO_ENABLE = "enable"

EXTRA_OEMESONFLAGS = "-Dmpdupnp=enabled"

do_install:append() {
    install -d ${D}${sysconfdir}
    install -m 0644 ${UNPACKDIR}/upmpdcli.conf ${D}${sysconfdir}/

    # Install UPnP device icon
    install -m 0644 ${UNPACKDIR}/icon.png ${D}${datadir}/upmpdcli/icon.png

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${S}/systemd/upmpdcli.service ${D}${systemd_system_unitdir}/

    # Create empty directories that upmpdcli expects
    install -d ${D}${datadir}/upmpdcli/cdplugins
    install -d ${D}${datadir}/upmpdcli/radio_scripts

    # Remove Python scripts that require python3 interpreter
    rm -rf ${D}${datadir}/upmpdcli/src_scripts
    rm -rf ${D}${datadir}/upmpdcli/rdpl2stream

    # Set ownership to upmpdcli user
    chown -R upmpdcli:upmpdcli ${D}${datadir}/upmpdcli
}

FILES:${PN} = "${bindir}/upmpdcli ${sbindir}/upmpdcli ${sysconfdir}/upmpdcli.conf* ${datadir}/upmpdcli ${systemd_system_unitdir}/upmpdcli.service"
