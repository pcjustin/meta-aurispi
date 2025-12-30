require recipes-core/images/core-image-minimal.bb

SUMMARY = "Auris audio initramfs image - the actual rootfs for RAM boot"

# This is the actual rootfs that gets packed as cpio.gz and loaded as initramfs
IMAGE_INSTALL:append = " \
    alsa-lib \
    alsa-utils \
    alsa-config \
    mpd \
    mpd-auris \
    upmpdcli \
    shairport-sync \
    avahi-daemon \
    libconfig \
    scx \
"
