SUMMARY = "AirPlay audio player with Audio Synchronisation"
DESCRIPTION = "Shairport Sync is an AirPlay audio receiver that allows audio to be played through ALSA, supporting synchronisation across multiple devices"
HOMEPAGE = "https://github.com/mikebrady/shairport-sync"
LICENSE = "MIT & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://COPYING;md5=ee2d008df96f0812830f2262b10ca8e0 \
                    file://LICENSES;md5=9f329b7b34fcd334fb1f8e2eb03d33ff"

DEPENDS = "alsa-lib libconfig popt avahi openssl autoconf-archive-native"
RDEPENDS:${PN} = "alsa-lib libconfig popt avahi-daemon openssl"

inherit useradd

USERADD_PACKAGES = "${PN}"
USERADD_PARAM:${PN} = "-r -s /bin/false -d /nonexistent -m -g audio shairport-sync"
GROUPADD_PARAM:${PN} = "-r audio"

SRC_URI = "git://github.com/mikebrady/shairport-sync.git;protocol=https;branch=master \
           file://0001-Remove-user-installation-dependency-from-systemv-and.patch \
           file://shairport-sync.conf \
           file://shairport-sync.service \
           file://shairport-pre-play.sh \
           file://shairport-post-play.sh"
SRCREV = "0b1c4391ffd398e7b145eb4b98416261380adeea"

inherit autotools systemd pkgconfig

SYSTEMD_SERVICE:${PN} = "shairport-sync.service"
SYSTEMD_AUTO_ENABLE = "enable"

EXTRA_OECONF = "\
    --with-alsa \
    --with-stdout \
    --with-ssl=openssl \
    --with-avahi \
    --with-metadata \
    --with-systemd \
"

# Skip the upstream install-exec-hook that installs config/service files
# We'll handle those ourselves via do_install:append
do_install:prepend() {
    # Disable the install-exec-hook by removing the hook target
    sed -i '/install-exec-hook/d' ${B}/Makefile 2>/dev/null || true
}

do_install:append() {
    # Install custom configuration
    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/sources/shairport-sync.conf ${D}${sysconfdir}/

    # Install custom systemd service (with CPU isolation)
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/sources/shairport-sync.service ${D}${systemd_system_unitdir}/

    # Install auto-switching scripts
    install -d ${D}${libexecdir}/shairport-sync
    install -m 0755 ${WORKDIR}/sources/shairport-pre-play.sh ${D}${libexecdir}/shairport-sync/
    install -m 0755 ${WORKDIR}/sources/shairport-post-play.sh ${D}${libexecdir}/shairport-sync/
}

FILES:${PN} = "\
    ${bindir}/shairport-sync \
    ${sysconfdir}/shairport-sync.conf \
    ${systemd_system_unitdir}/shairport-sync.service \
    ${libexecdir}/shairport-sync/shairport-pre-play.sh \
    ${libexecdir}/shairport-sync/shairport-post-play.sh \
"
